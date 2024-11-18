package west2project;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@EnableWebSocket
@EnableRabbit
@EnableScheduling
@SpringBootApplication
public class West2ProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(West2ProjectApplication.class, args);
    }

}
