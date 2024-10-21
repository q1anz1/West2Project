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
import west2project.pojo.DTO.LikeDTO;
import west2project.utils.RedisUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveDBTask {
    private final RedisUtil redisUtil;
    private final InteractionMapper interactionMapper;

    @Scheduled(fixedRate = 50000)//单位为毫秒
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
    @Scheduled(fixedRate = 50000)//单位为毫秒
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
    @Scheduled(fixedRate = 50000)//单位为毫秒
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
    @Scheduled(fixedRate = 50000)//单位为毫秒
    public void saveLikeVideo(){
        saveLike("likeVideo",Contexts.CACHE_VIDEO_LIKE,interactionMapper::saveVideoLike);
    }
    @Scheduled(fixedRate = 50000)//单位为毫秒
    public void saveDislikeVideo(){
        Object obj =redisUtil.leftPopList(Contexts.TASK,"dislikeVideo");
        if (obj != null){
            log.info("dislike队列-1");
            Long userId = Long.parseLong(obj.toString());
            //获得redis中的
            List<Long> targetIdList = redisUtil.findJsonList(Contexts.CACHE_VIDEO_LIKE, userId, Long.class);
            if (targetIdList.isEmpty()){
                return;
            }
            for (int i = 0; i < targetIdList.size(); i++) {
                interactionMapper.deleteVideoLike(targetIdList.get(i), userId);
            }
            //循环调用
            saveDislikeVideo();
        }
    }

    public void saveLike(String key, String cacheKey, Consumer<List<LikeDTO>> db){
        Object obj =redisUtil.leftPopList(Contexts.TASK,key);
        if (obj != null){
            log.info("like队列-1");
            Long userId = Long.parseLong(obj.toString());
            //获得redis中的
            List<Long> targetIdList = redisUtil.findJsonList(cacheKey, userId, Long.class);
            if (targetIdList.isEmpty()){
                return;
            }
            //保存到数据库
            List<LikeDTO> likeDTOList = new ArrayList<>();
            LikeDTO likeDTO = new LikeDTO();
            for (Long aLong : targetIdList) {
                likeDTO.setTargetId(aLong);
                likeDTO.setUserId(userId);
                likeDTOList.add(likeDTO);
            }
            db.accept(likeDTOList);
            //循环调用
            saveLike(key,cacheKey,db);
        }
    }
}
