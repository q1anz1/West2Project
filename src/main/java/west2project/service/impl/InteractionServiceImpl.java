package west2project.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import west2project.context.RedisContexts;
import west2project.exception.ArgsInvalidException;
import west2project.mapper.InteractionMapper;
import west2project.pojo.DO.video.CommentDO;
import west2project.pojo.DO.video.VideoDO;
import west2project.pojo.VO.PageBean;
import west2project.result.Result;
import west2project.service.InteractionService;
import west2project.util.JwtUtil;
import west2project.util.PageUtil;
import west2project.util.RedisUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final HttpServletRequest httpServletRequest;
    private final InteractionMapper interactionMapper;
    private final RedisUtil redisUtil;
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public Result like(Long videoId, Long commentId, Integer type) {
        if ((videoId == null && commentId == null) || (videoId != null && commentId != null) || (type != 1 && type != 2)) {
            throw new ArgsInvalidException("操作有误");
        }
        Long userId = JwtUtil.getUserId(httpServletRequest);
        Result result;
        lock.lock();
        try {
            if (videoId != null) {//视频的操作
                Result videoResult = RedisUtil.findJsonWithCache(RedisContexts.CACHE_VIDEODO, videoId, VideoDO.class,
                        interactionMapper::findVideoDO, RedisContexts.CACHE_VIDEODO_TTL);
                VideoDO videoDO = (VideoDO) videoResult.getData();
                if (videoDO == null || videoDO.getVideoUrl() == null) {
                    throw new ArgsInvalidException("视频不存在");
                }
                //从缓存中寻找是否点赞(某个用户点赞过的视频列表)
                result = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_VIDEO_LIKE, userId, Long.class, interactionMapper::findVideoLike,
                        RedisContexts.CACHE_VIDEO_COMMENT_LIKE_TTL);
                List<Long> followList = (List<Long>) result.getData();
                if (type == 1) {//视频点赞
                    if (followList.contains(videoId)) {
                        throw new ArgsInvalidException("已点赞");
                    }
                    //若未点赞
                    //存储已点赞cache
                    followList.add(videoId);
                    RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_VIDEO_LIKE, userId, followList, RedisContexts.CACHE_VIDEO_COMMENT_LIKE_TTL);
                    // 点赞数+1
                    videoDO.setLikeCount(videoDO.getLikeCount() + 1);

                    redisUtil.rightPushList(RedisContexts.TASK,"likeVideo",userId);

                } else {//视频取消点赞
                    if (!followList.contains(videoId)) {
                        throw new ArgsInvalidException("未点赞，无法取消点赞");
                    }
                    //若已点赞
                    //存储已点赞cache
                    followList.remove(videoId);
                    RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_VIDEO_LIKE, userId, followList, RedisContexts.CACHE_VIDEO_COMMENT_LIKE_TTL);
                    // 点赞数-1
                    videoDO.setLikeCount(videoDO.getLikeCount() - 1);
                    redisUtil.rightPushList(RedisContexts.TASK,"dislikeVideo",userId);
                }
                //加入写入数据库队列

                redisUtil.rightPushList(RedisContexts.TASK, "visitVideo", videoId);
            } else {//评论操作
                //判断评论是否存在
                //
                Result commentResult = RedisUtil.findJsonWithCache(RedisContexts.CACHE_COMMENTDO, commentId, CommentDO.class,
                        interactionMapper::findCommentDO, RedisContexts.CACHE_COMMENTDO_TTL);
                CommentDO commentDO = (CommentDO) commentResult.getData();
                if (commentDO == null || commentDO.getContent() == null) {
                    throw new ArgsInvalidException("评论不存在");
                }
                //从缓存中寻找是否点赞过
                result = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_COMMENT_LIKE, userId, Long.class, interactionMapper::findCommentLike,
                        RedisContexts.CACHE_VIDEO_COMMENT_LIKE_TTL);
                List<Long> followList = (List<Long>) result.getData();
                if (type == 1) {//评论点赞
                    if (followList.contains(commentId)) {
                        throw new ArgsInvalidException("已点赞");
                    }
                    //若未点赞
                    //存储已点赞cache
                    followList.add(commentId);
                    RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_COMMENT_LIKE, userId, followList, RedisContexts.CACHE_VIDEO_COMMENT_LIKE_TTL);
                    //点赞数+1
                    followList.add(commentId);
                    redisUtil.rightPushList(RedisContexts.TASK,"likeComment",userId);
                } else {//评论取消点赞
                    if (!followList.contains(commentId)) {
                        throw new ArgsInvalidException("未点赞，无法取消点赞");
                    }
                    //存储关注cache
                    followList.remove(commentId);
                    RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_COMMENT_LIKE, userId.toString(), followList, RedisContexts.CACHE_VIDEO_COMMENT_LIKE_TTL);
                    //点赞数-1
                    followList.remove(commentId);
                    redisUtil.rightPushList(RedisContexts.TASK,"dislikeComment",userId);
                }
                //加入写入数据库队列

                redisUtil.rightPushList(RedisContexts.TASK, "visitComment", commentDO.getId());

            }
        } finally {
            lock.unlock();
        }
        return Result.success();
    }

    @Override
    public Result videoLikeList(Long userId, Integer pageNum, Integer pageSize, Integer type) {
        if (type != 1 && type != 2) {
            throw new ArgsInvalidException("操作有误");
        }
        List<Long> list;
        //1为视频
        if (type == 1) {
            Result videoResult = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_VIDEO_LIKE, userId, Long.class, interactionMapper::selectVideoLikeList,
                    RedisContexts.CACHE_FOLLOW_FAN_TTL);
            list = (List<Long>) videoResult.getData();
        } else {
            Result commentResult = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_COMMENT_LIKE, userId, Long.class, interactionMapper::selectCommentLikeList,
                    RedisContexts.CACHE_FOLLOW_FAN_TTL);
            list = (List<Long>) commentResult.getData();
        }
        //判断是否为空
        if (list.isEmpty()) {
            throw new ArgsInvalidException("无点赞对象");
        }
        //分页查找
        PageBean pageBean = new PageBean<>();
        pageBean.setData(PageUtil.page(list, pageSize, pageNum));
        if (pageBean.getData() == null) {
            throw new ArgsInvalidException("分页参数非法");
        }
        pageBean.setTotalPage((long) pageBean.getData().size());
        return Result.success(pageBean);
    }

    @Override
    public Result publishComment(Long videoId, Long parentId, String context) {
        //内容已经非空，不用检测
        if ((videoId != null && parentId != null) || (videoId == null && parentId == null)) {
            throw new ArgsInvalidException("操作有误");
        }
        //获取当前用户id
        Long userId = JwtUtil.getUserId(httpServletRequest);
        lock.lock();
        try {
            if (videoId != null) {//评论视频
                //检测视频是否存在
                Result result = RedisUtil.findJsonWithCache(RedisContexts.CACHE_VIDEODO, videoId, VideoDO.class,
                        interactionMapper::findVideoDO, RedisContexts.CACHE_VIDEODO_TTL);
                VideoDO videoDO = (VideoDO) result.getData();
                if (videoDO == null || videoDO.getVideoUrl() == null) throw new ArgsInvalidException("操作有视频不存在误");
                //视频存在
                CommentDO commentDO = new CommentDO(userId, context);
                commentDO.setVideoId(videoId);
                interactionMapper.saveCommentDO(commentDO);
                //mapper已经自动将主键id存入DO
                //存储到redis中
                RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_COMMENTDO, commentDO.getId(), commentDO, RedisContexts.CACHE_COMMENTDO_TTL);
                // 增加视频的评论数
                videoDO.setCommentCount(videoDO.getCommentCount() + 1);
                RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_COMMENTDO, videoDO.getId(), videoDO, RedisContexts.CACHE_COMMENTDO_TTL);
                //加入存数据库队列
                redisUtil.rightPushList(RedisContexts.TASK, "visitComment", commentDO.getId());
                redisUtil.rightPushList(RedisContexts.TASK, "visitVideo", videoDO.getId());
                //更新视频的评论列表
                List<CommentDO> commentList = RedisUtil.findJsonList(RedisContexts.CACHE_COMMENT_LIST_VIDEO, videoId, CommentDO.class);
                commentList.add(commentDO);
                RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_COMMENT_LIST_VIDEO, videoId, commentList, RedisContexts.CACHE_COMMENT_LIST_TTL);
            } else {//评论评论

                //检测评论是否存在
                Result result = RedisUtil.findJsonWithCache(RedisContexts.CACHE_COMMENTDO, parentId, CommentDO.class,
                        interactionMapper::findCommentDO, RedisContexts.CACHE_COMMENTDO_TTL);
                CommentDO parentDO = (CommentDO) result.getData();
                if ((parentDO == null || parentDO.getContent() == null) || (parentDO.getDeletedAt() != null)) {
                    throw new ArgsInvalidException("评论不存在");
                }
                //评论存在
                CommentDO commentDO = new CommentDO(userId, context);
                commentDO.setParentId(parentId);
                interactionMapper.saveCommentDO(commentDO);
                //mapper已经自动将主键id存入DO
                //存储到redis中
                RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_COMMENTDO, commentDO.getId(), commentDO, RedisContexts.CACHE_COMMENTDO_TTL);
                // 增加父的评论数
                parentDO.setChildCount(parentDO.getChildCount() + 1);
                RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_COMMENTDO, parentDO.getId(), parentDO, RedisContexts.CACHE_COMMENTDO_TTL);
                //加入存数据库队列
                redisUtil.rightPushList(RedisContexts.TASK, "visitComment", commentDO.getId());
                redisUtil.rightPushList(RedisContexts.TASK, "visitComment", parentDO.getId());
                //更新评论的评论列表
                List<CommentDO> commentList = RedisUtil.findJsonList(RedisContexts.CACHE_COMMENT_LIST_COMMENT, parentId, CommentDO.class);
                commentList.add(commentDO);
                RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_COMMENT_LIST_COMMENT, parentId, commentList, RedisContexts.CACHE_COMMENT_LIST_TTL);
            }
        } finally {
            lock.unlock();
        }
        return Result.success();
    }

    @Override
    public Result deleteComment(Long commentId) {
        lock.lock();
        try {
            Result result = RedisUtil.findJsonWithCache(RedisContexts.CACHE_COMMENTDO, commentId, CommentDO.class,
                    interactionMapper::findCommentDO, RedisContexts.CACHE_COMMENTDO_TTL);
            CommentDO commentDO = (CommentDO) result.getData();
            //评论是否存在
            if (commentDO == null || commentDO.getContent() == null) {
                throw new ArgsInvalidException("未找到评论");
            }
            if (commentDO.getDeletedAt() != null) {
                throw new ArgsInvalidException("未找到评论");
            }
            //核对评论是否是当前用户的
            Long userId = JwtUtil.getUserId(httpServletRequest);
            if (!Objects.equals(userId, commentDO.getUserId())) {
                throw new ArgsInvalidException("无权限删除");
            }
            //逻辑删除
            commentDO.setDeletedAt(new Date(System.currentTimeMillis()));
            RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_COMMENTDO, commentId, commentDO, RedisContexts.CACHE_COMMENTDO_TTL);
            //加入任务
            redisUtil.rightPushList(RedisContexts.TASK, "visitComment", commentDO.getId());
        } finally {
            lock.unlock();
        }
        return Result.success();
    }

    @Override
    public Result commentList(Long videoId, Long commentId, Integer pageSize, Integer pageNum) {
        if ((videoId == null && commentId == null) || (videoId != null && commentId != null)) {
            throw new ArgsInvalidException("操作错误");
        }
        Result result;
        if (videoId != null) {//视频的评论
            result = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_COMMENT_LIST_VIDEO, videoId, CommentDO.class,
                    interactionMapper::selectVideoCommentList, RedisContexts.CACHE_COMMENT_LIST_TTL);
        } else {//评论的评论
            result = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_COMMENT_LIST_COMMENT, commentId, CommentDO.class,
                    interactionMapper::selectCommentCommentList, RedisContexts.CACHE_COMMENT_LIST_TTL);
        }
        List<CommentDO> list1 = (List<CommentDO>) result.getData();
        List<CommentDO> list = new ArrayList<>();
        //去除被逻辑删除的评论
        for (CommentDO commentDO : list1) {
            if(commentDO.getDeletedAt() == null){
                list.add(commentDO);
            }
        }
        if (list.isEmpty()) {
            throw new ArgsInvalidException("无内容");
        }
        PageBean<CommentDO> pageBean = new PageBean<>();
        pageBean.setData(PageUtil.page(list, pageSize, pageNum));
        if (pageBean.getData() == null) {
            throw new ArgsInvalidException("分页参数非法");
        }
        pageBean.setTotalPage((long) pageBean.getData().size());
        return Result.success(list);
    }

}
