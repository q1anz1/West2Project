package west2project.rabbitmq;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import west2project.mapper.MessageMapper;
import west2project.pojo.DO.chat.MessageDO;
import west2project.pojo.DTO.chat.MQSaveChatMsgDTO;
import west2project.util.RedisUtil;

import java.util.concurrent.TimeUnit;

import static west2project.context.RedisContexts.REDIS_MESSAGE_UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveChatMsgQueue {
    private final RabbitTemplate rabbitTemplate;
    private final MessageMapper messageMapper;

    public void sendSaveChatMsgQueue(MQSaveChatMsgDTO mqSaveChatMsgDTO) {
        rabbitTemplate.convertAndSend("save.chat.queue",mqSaveChatMsgDTO);
    }

    @RabbitListener(queues = "save.chat.queue")
    public void listenSaveChatMsgQueue(String msg) throws Exception {
        log.info("{}队列收到消息：{}","save.chat.queue",msg);
        MQSaveChatMsgDTO dto = JSONUtil.toBean(msg, MQSaveChatMsgDTO.class);
        MessageDO DO = dto.getMessageDO();
        if (DO == null) throw new Exception("messageDO为空");
        // 验证UUID防止重复
        String uuid = dto.getUuid();
        if (uuid == null) {
            log.error("消息uuid为空");
            return;
        }
        if (RedisUtil.find(REDIS_MESSAGE_UUID, uuid) != null){
            log.warn("消息重复:{}", uuid);
            return;
        }
        RedisUtil.writeDataWithTTL(REDIS_MESSAGE_UUID, uuid, "", 5L,TimeUnit.SECONDS);
        messageMapper.saveMessageDO(DO.getUserId(), DO.getText(),
                DO.getPictureUrl(), DO.getToUserId(), DO.getGroupId(), DO.getCreatedAt());
    }
}
