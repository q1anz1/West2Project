package west2project.rabbitmq;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import west2project.mapper.SessionMapper;
import west2project.pojo.DO.chat.SessionDO;
import west2project.util.RedisUtil;

import static west2project.context.RedisContexts.CACHE_SESSIONDO;
import static west2project.context.RedisContexts.CACHE_SESSIONDO_TTL;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateSessionQueue {
    private final RabbitTemplate rabbitTemplate;
    private final SessionMapper sessionMapper;

    public void sendUpdateSessionQueue(SessionDO sessionDO) {
        rabbitTemplate.convertAndSend("session.update.queue", sessionDO);
    }

    @RabbitListener(queues = "session.update.queue")
    private void listenUpdateSessionQueue(String msg) {
        SessionDO sessionDO = JSONUtil.toBean(msg, SessionDO.class);
        RedisUtil.writeJsonWithTTL(CACHE_SESSIONDO, sessionDO.getId(), sessionDO,CACHE_SESSIONDO_TTL);
        sessionMapper.updateSession(sessionDO.getId(), sessionDO.getLastMessage(), sessionDO.getUpdatedAt());
    }
}
