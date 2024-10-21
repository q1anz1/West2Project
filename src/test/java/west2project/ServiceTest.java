package west2project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import west2project.context.Contexts;
import west2project.utils.RedisUtil;

import java.util.Random;

@SpringBootTest
public class ServiceTest {
    @Autowired
    private RedisUtil  redisUtil;

    @Test
    public void test(){
        String code=randomCapitalLetter(6);
        String email ="123@email";
        redisUtil.writeDataWithTTL("email:code:",email,code, Contexts.REDIS_EMAIL_CODE_TTL);
    }
    public String randomCapitalLetter(int n){
        Random random = new Random();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char randomChar = (char) (random.nextInt(26) + 65);
            sb.append(randomChar);
        }
        return sb.toString();
    }
}
