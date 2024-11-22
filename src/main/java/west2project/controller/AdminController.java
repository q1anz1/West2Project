package west2project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import west2project.result.Result;
import west2project.service.AdminService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @DeleteMapping("/admin/user/delete")
    public Result<?> deleteUser(@RequestParam("user_id") Long userId) {
        log.info("封禁用户：{}", userId);
        return adminService.deleteUser(userId);
    }

    @DeleteMapping("/admin/delete/video")
    public Result<?> deleteVideo(@RequestParam("video_id") Long videoId) {
        log.info("封禁视频：{}", videoId);
        return adminService.deleteVideo(videoId);
    }

    @GetMapping("/admin/review/list/get")
    public Result<?> getReviewVideoList() {
        return adminService.getReviewVideoList();
    }

    @GetMapping("/admin/review/{video_id}")
    public Result<?> reviewVideo(@PathVariable("video_id")Long videoId){
        return adminService.reviewVideo(videoId);
    }

    @PostMapping("/admin/pass")
    public Result<?> pass(@RequestParam("video_id")Long videoId, @RequestParam("action")Integer action) {
        return adminService.pass(videoId, action);
    }
}
