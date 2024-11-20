package west2project.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import west2project.context.RedisContexts;
import west2project.mapper.InteractionMapper;
import west2project.pojo.DO.video.CommentDO;
import west2project.pojo.DO.video.VideoDO;
import west2project.pojo.DTO.LikeDTO;
import west2project.util.RedisUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveDBQueues {
    private final RabbitTemplate rabbitTemplate;
    private final InteractionMapper interactionMapper;

    public void sendVisitVideoQueue(Long videoId) {
        rabbitTemplate.convertAndSend("visit.video.queue",videoId);
    }

    @RabbitListener(queues = "visit.video.queue")
    private void listenVisitVideoQueue(String msg) {
        log.info("{}队列收到消息：{}","visit.video.queue",msg);
        Long videoId = Long.valueOf(msg);
        // 获得redis中的video
        VideoDO videoDO = RedisUtil.findJson(RedisContexts.CACHE_VIDEODO,videoId, VideoDO.class);
        // 获取 点赞，评论，观看量,保存到数据库
        interactionMapper.updateVideoVisitCount(videoId,videoDO.getVisitCount(),videoDO.getLikeCount(),videoDO.getCommentCount());
    }

    public void sendVisitCommentQueue(Long commentId) {
        rabbitTemplate.convertAndSend("visit.comment.queue",commentId);
    }

    @RabbitListener(queues = "visit.comment.queue")
    private void listenVisitCommentQueue(String msg) {
        log.info("{}队列收到消息：{}","visit.comment.queue",msg);
        Long commentId = Long.valueOf(msg);
        //获得redis中的comment
        CommentDO commentDO = RedisUtil.findJson(RedisContexts.CACHE_COMMENTDO,commentId, CommentDO.class);
        //保存到数据库
        interactionMapper.updateCommentVisitCount(commentId,commentDO.getLikeCount(),commentDO.getChildCount(),commentDO.getDeletedAt());
    }

    public void sendLikeVideoQueue(Long userId) {
        rabbitTemplate.convertAndSend("like.video.queue",userId);
    }

    @RabbitListener(queues = "like.video.queue")
    private void listenLikeVideoQueue(String msg) {
        log.info("{}队列收到消息：{}","like.video.queue",msg);
        Long userId = Long.valueOf(msg);
        //获得redis中的
        List<Long> targetIdList = RedisUtil.findJsonList(RedisContexts.CACHE_VIDEO_LIKE, userId, Long.class);
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
        interactionMapper.saveVideoLike(likeDTOList);
    }

    public void sendDislikeVideoQueue(Long userId) {
        rabbitTemplate.convertAndSend("dislike.video.queue", userId);
    }

    @RabbitListener(queues = "dislike.video.queue")
    private void listenDislikeVideoQueue(String msg) {
        log.info("{}队列收到消息：{}","dislike.video.queue",msg);
        Long userId = Long.valueOf(msg);
        //获得redis中的
        List<Long> targetIdList = RedisUtil.findJsonList(RedisContexts.CACHE_VIDEO_LIKE, userId, Long.class);
        if (targetIdList.isEmpty()){
            return;
        }
        for (Long aLong : targetIdList) {
            interactionMapper.deleteVideoLike(aLong, userId);
        }
    }
}
