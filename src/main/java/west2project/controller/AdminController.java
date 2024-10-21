package west2project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import west2project.result.Result;
import west2project.service.AdminService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @DeleteMapping("/admin/user/delete")
    public Result deleteUser(@RequestParam("user_id") Long userId){
        log.info("封禁用户：{}",userId);
        return adminService.deleteUser(userId);
    }
    @DeleteMapping("/admin/delete/video")
    public Result deleteVideo(@RequestParam("video_id") Long videoId){
        log.info("封禁视频：{}",videoId);
        return adminService.deleteVideo(videoId);
    }
}
