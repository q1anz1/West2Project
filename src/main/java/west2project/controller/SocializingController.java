package west2project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import west2project.result.Result;
import west2project.service.SocializingService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SocializingController {
    private final SocializingService socializingService;

    @PostMapping("/relation/action")
    public Result action(@RequestParam("to_user_id") Long toUserId,@RequestParam("action_type")Integer type){
        log.info("关注或取关用户: {},{} 。",toUserId,type);
        return socializingService.action(toUserId,type);
    }

    @GetMapping("following/list")
    public Result followList(@RequestParam("user_id") Long userId,@RequestParam("page_num") Integer pageNum,
                       @RequestParam("page_size")Integer pageSize){
        log.info("查询用户id为：{} 的关注列表，第{}页,每页{}个。",userId,pageNum,pageSize);
        return socializingService.followList(userId,pageNum,pageSize);
    }

    @GetMapping("/follower/list")
    public Result fanList(@RequestParam("user_id") Long userId,@RequestParam("page_num") Integer pageNum,
                          @RequestParam("page_size")Integer pageSize){
        log.info("查询用户id为：{} 的粉丝列表，第{}页,每页{}个。",userId,pageNum,pageSize);
        return socializingService.fanList(userId,pageNum,pageSize);
    }

    @GetMapping("/friends/list")
    public Result friendList(@RequestParam("page_num") Integer pageNum, @RequestParam("page_size")Integer pageSize){
        log.info("查询用户id为：{} 的好友列表，第{}页,每页{}个。","当前用户",pageNum,pageSize);
        return socializingService.friendList(pageNum,pageSize);
    }
}
