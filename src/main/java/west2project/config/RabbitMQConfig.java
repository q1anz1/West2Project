package west2project.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 设置消息转换器为Jackson2JsonMessageConverter
        MessageConverter messageConverter = new Jackson2JsonMessageConverter();
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue getFlushFriendQueue() {
        return new Queue("flush.friend.queue", true, false, false);
    }

    @Bean
    public Queue getSaveChatMsgQueue() {
        return new Queue("save.chat.queue", true, false, false);
    }

    @Bean
    public Queue getVisitVideoQueue() {
        return new Queue("visit.video.queue", true, false, false);
    }

    @Bean
    public Queue getVisitCommentQueue() {
        return new Queue("visit.comment.queue", true, false, false);
    }

    @Bean
    public Queue getLikeVideoQueue() {
        return new Queue("like.video.queue", true, false, false);
    }

    @Bean
    public Queue getDislikeVideoQueue() {
        return new Queue("dislike.video.queue", true, false, false);
    }

    @Bean
    public Queue getChatMessageQueue() {
        return new Queue("chat.message.queue", true, false, false);
    }

    @Bean
    public Queue getUpdateSession() {
        return new Queue("session.update.queue", true, false, false);
    }
}
