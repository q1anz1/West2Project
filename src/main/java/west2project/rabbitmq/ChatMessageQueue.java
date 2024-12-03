package west2project.rabbitmq;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import west2project.mapper.GroupUserMapper;
import west2project.pojo.DTO.chat.MQFullMessageDTO;
import west2project.pojo.VO.chat.message.ChatMsg;
import west2project.pojo.VO.chat.message.FullMessage;
import west2project.result.Result;
import west2project.util.ChannelUtil;
import west2project.util.RedisUtil;

import java.util.ArrayList;
import java.util.List;

import static west2project.context.RedisContexts.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageQueue {
  /*  private final RabbitTemplate rabbitTemplate;
    private final GroupUserMapper groupUserMapper;
    private final RedisUtil redisUtil;
    public void sendChatMessageQueue(FullMessage<ChatMsg> fullMessage, Long targetId, Boolean toUser) {
        MQFullMessageDTO mqFullMessageDTO = new MQFullMessageDTO(toUser, targetId, fullMessage);
        rabbitTemplate.convertAndSend("chat.message.queue",mqFullMessageDTO);
    }

    @RabbitListener(queues = "chat.message.queue")
    private void listenChatMessageQueue(String msg) {
        log.info("{}队列收到消息：{}","chat.message.queue",msg);
        // 通过websocket发送给目标用户，如果发送成功返回true
        MQFullMessageDTO mqFullMessageDTO = JSONUtil.toBean(msg, MQFullMessageDTO.class);
        FullMessage<ChatMsg> fullMessage = mqFullMessageDTO.getFullMessage();
        if (mqFullMessageDTO.getToAUser()) {
            // 发送给好友
            Long toUserId = mqFullMessageDTO.getTargetId();
            // 通过websocket发送给目标用户，如果发送成功返回true
            boolean isSuccess = ChannelUtil.sendPersonalMsg(Result.success(fullMessage), toUserId);
            // 好友不在线
            if (!isSuccess) {
                // 将消息存到redis
                redisUtil.rightPushList(REDIS_UNREAD_MESSAGE, toUserId, fullMessage);
            }
        } else {
            // 发送到群
            Long groupId = mqFullMessageDTO.getTargetId();
            // 通过websocket发送给目标用户，如果发送成功返回true
            List<Long> successUserIdList = ChannelUtil.sendGroupMessage(Result.success(fullMessage), groupId);
            // 群友不在线
            // 获得群友id列表
            Result<?> result = RedisUtil.findJsonListWithCache(CACHE_GROUP_USER, groupId, Long.class, groupUserMapper::selectGroupUserIdByGroupId, CACHE_GROUP_USER_TTL);
            List<Long> userIdList = (List<Long>) result.getData();
            // 获得发送未成功的群友id列表
            successUserIdList.forEach(userIdList::remove);
            List<Long> unsuccessUserIdList = new ArrayList<>(successUserIdList);
            // 存储到redis
            unsuccessUserIdList.forEach(userid -> {
                redisUtil.rightPushList(REDIS_UNREAD_MESSAGE, userid, fullMessage);
            });
        }
    }*/
}
