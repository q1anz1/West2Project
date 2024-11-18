package west2project.rabbitmq;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import west2project.mapper.MessageMapper;
import west2project.pojo.DO.chat.MessageDO;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveChatMsgQueue {
    private final RabbitTemplate rabbitTemplate;
    private final MessageMapper messageMapper;

    public void sendSaveChatMsgQueue(MessageDO messageDO) {
        rabbitTemplate.convertAndSend("save.chat.queue",messageDO);
    }

    @RabbitListener(queues = "save.chat.queue")
    public void listenSaveChatMsgQueue(String msg) throws Exception {
        log.info("{}队列收到消息：{}","save.chat.queue",msg);
        MessageDO DO = JSONUtil.toBean(msg, MessageDO.class);
        if (DO == null) throw new Exception("messageDO为空");
        messageMapper.saveMessageDO(DO.getUserId(), DO.getText(),
                DO.getPictureUrl(), DO.getToUserId(), DO.getGroupId(), DO.getCreatedAt());
    }
}
