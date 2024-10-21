package west2project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class West2ProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(West2ProjectApplication.class, args);
    }

}
