package west2project.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import west2project.context.Contexts;
import west2project.mapper.InteractionMapper;
import west2project.pojo.DO.users.UserDO;
import west2project.pojo.DO.videos.CommentDO;
import west2project.pojo.DO.videos.VideoDO;
import west2project.utils.RedisUtil;
@Slf4j
@Component
@RequiredArgsConstructor
public class SaveDBTask {
    private final RedisUtil redisUtil;
    private final InteractionMapper interactionMapper;

    @Scheduled(fixedRate = 5000)//单位为毫秒
    public void saveVideoVisitCountLikeCount(){
        Object obj =redisUtil.leftPopList(Contexts.TASK,"visitVideo");
        if (obj != null){
            log.info("visitVideo队列-1");
            Long videoId = Long.parseLong(obj.toString());
            //获得redis中的video
            VideoDO videoDO = redisUtil.findJson(Contexts.CACHE_VIDEODO,videoId, VideoDO.class);
            //获取 点赞，评论，观看量,保存到数据库
            interactionMapper.updateVideoVisitCount(videoId,videoDO.getVisitCount(),videoDO.getLikeCount(),videoDO.getCommentCount());
            //循环调用
            saveVideoVisitCountLikeCount();
        }
    }
    @Scheduled(fixedRate = 5000)//单位为毫秒
    public void saveCommentVisitCountLikeCount(){
        Object obj =redisUtil.leftPopList(Contexts.TASK,"visitComment");
        if (obj != null){
            log.info("visitComment队列-1");
            Long commentId = Long.parseLong(obj.toString());
            //获得redis中的comment
            CommentDO commentDO = redisUtil.findJson(Contexts.CACHE_COMMENTDO,commentId, CommentDO.class);
            //保存到数据库
            interactionMapper.updateCommentVisitCount(commentId,commentDO.getLikeCount(),commentDO.getChildCount(),commentDO.getDeletedAt());
            //循环调用
            saveCommentVisitCountLikeCount();
        }
    }
    @Scheduled(fixedRate = 5000)//单位为毫秒
    public void saveUserInfo(){
        Object obj =redisUtil.leftPopList(Contexts.TASK,"visitUserInfo");
        if (obj != null){
            log.info("visitUserInfo队列-1");
            Long userId = Long.parseLong(obj.toString());
            //获得redis中的
            UserDO userDO = redisUtil.findJson(Contexts.CACHE_USERDO,userId, UserDO.class);
            //保存到数据库
            interactionMapper.updateUserVisitCount(userId,userDO.getAvatarUrl());
            //循环调用
            saveVideoVisitCountLikeCount();
        }
    }
}
