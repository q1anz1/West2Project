package west2project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import west2project.result.Result;
import west2project.service.InteractionService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;

    @PostMapping("/like/action")
    public Result like(@RequestParam(value ="video_id",required = false)Long videoId,
                       @RequestParam(value ="comment_id",required = false)Long commentId,
                       @RequestParam("action_type")Integer type){
        log.info("点赞或取消点赞");
        return interactionService.like(videoId,commentId,type);
    }

    @GetMapping("/like/list")
    public Result likeList(@RequestParam("user_id") Long userId,
                           @RequestParam("page_num") Integer pageNum,
                           @RequestParam("page_size")Integer pageSize,
                           @RequestParam("type")Integer type){
        log.info("查询用户id为：{} 的点赞，第{}页,每页{}个。",userId,pageNum,pageSize);
        return interactionService.videoLikeList(userId,pageNum,pageSize,type);
    }

    @PostMapping("/comment/publish")
    public Result publishComment(@RequestParam(value = "video_id",required = false)Long videoId,
                                 @RequestParam(value = "comment_id",required = false)Long commentId,
                                 @RequestParam("content")String content){
        log.info("发布评论");
        return interactionService.publishComment(videoId,commentId,content);
    }

    @DeleteMapping("/comment/delete")
    public Result deleteComment(@RequestParam("comment_id") Long commentId){
        log.info("删除评论：{}",commentId);
        return interactionService.deleteComment(commentId);
    }


    @GetMapping("/comment/list")
    public Result commentList(@RequestParam(value = "video_id",required = false)Long videoId,
                              @RequestParam(value = "comment_id",required = false)Long commentId,
                              @RequestParam("page_size")Integer pageSize,
                              @RequestParam("page_num")Integer pageNum){
        log.info("查看评论列表 视频id:{},评论id:{},分页参数:{}{}",videoId,commentId,pageSize,pageNum);
        return interactionService.commentList(videoId,commentId,pageSize,pageNum);
    }
}
