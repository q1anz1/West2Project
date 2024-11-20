package west2project.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import west2project.context.RedisContexts;
import west2project.mapper.InteractionMapper;
import west2project.pojo.DO.user.UserDO;
import west2project.pojo.DTO.LikeDTO;
import west2project.util.RedisUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveDBTask {
    private final InteractionMapper interactionMapper;
    private final RedisUtil redisUtil;

/*    @Scheduled(fixedRate = 50000)//单位为毫秒
    public void saveUserInfo(){
        Object obj = redisUtil.leftPopList(RedisContexts.TASK,"visitUserInfo",Object.class);
        if (obj != null){
            log.info("visitUserInfo队列-1");
            Long userId = Long.parseLong(obj.toString());
            //获得redis中的
            UserDO userDO = RedisUtil.findJson(RedisContexts.CACHE_USERDO,userId, UserDO.class);
            //保存到数据库
            interactionMapper.updateUserVisitCount(userId,userDO.getAvatarUrl());
            //循环调用
        }
    }*/

}
