package west2project.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import west2project.util.ChatServerUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlushFriendQueue {
    private final RabbitTemplate rabbitTemplate;
    private final ChatServerUtil chatServerUtil;

    public void sendFlushFriendQueue(Long userId) {
        rabbitTemplate.convertAndSend("flush.friend.queue",userId.toString());
    }

    @RabbitListener(queues = "flush.friend.queue")
    public void listenFlushFriendQueue(String msg) {
        log.info("{}队列收到消息：{}","flush.friend.queue",msg);
        Long userId = Long.valueOf(msg);
        chatServerUtil.flushFriend(userId);
    }
}
