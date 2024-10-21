package west2project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import west2project.result.Result;
import west2project.service.VideoService;

import java.util.Date;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @PostMapping("/video/publish")
    public Result publish(@RequestParam("data")MultipartFile video,@RequestParam("title")String title,
                          @RequestParam("cover")MultipartFile image,
                          @RequestParam("description")String description){
            log.info("上传视频，标题：{}",title);
            return videoService.publish(video, title, image,description);
    }

    @GetMapping("/video/list")
    public Result videoPublishList(@RequestParam("user_id") Long userId,@RequestParam("page_num") Integer pageNum,
                                   @RequestParam("page_size")Integer pageSize){
        log.info("查询用户id为：{} 的发布视频列表，第{}页,每页{}个。",userId,pageNum,pageSize);
        return videoService.videoPublishList(userId,pageNum,pageSize);
    }

    @PostMapping("/video/search")
    public Result searchVideo(@RequestParam(value = "keywords",required = false)String keywords,
                         @RequestParam("page_size")Integer pageSize,
                         @RequestParam("page_num")Integer pageNum,
                         @RequestParam(value = "from_date",required = false)Integer fromDate,
                         @RequestParam(value = "to_date",required = false)Integer toDate,
                         @RequestParam(value = "username",required = false)String username){
        log.info("查询视频");
        return videoService.searchVideo(keywords,pageSize,pageNum,fromDate,toDate,username);
    }

    @GetMapping("/video/feed/")
    public Result videoFeed(@RequestParam("video_id") Long videoId){
        log.info("查看视频：{}",videoId);
        return videoService.videoFeed(videoId);
    }

    @GetMapping("/video/popular")
    public Result popular(@RequestParam("page_num") Integer pageNum,
                          @RequestParam("page_size")Integer pageSize){
        log.info("获得排行榜");
        return videoService.popular(pageNum,pageSize);
    }
}
