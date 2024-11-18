package west2project.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import west2project.chat.service.ChatService;
import west2project.result.Result;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/message/send")
    public Result<?> sendMessage(@RequestParam(value = "user_id", required = false)Long userId,
                                 @RequestParam(value = "group_id", required = false)Long groupId,
                                 @RequestParam(value = "text", required = false)String text,
                                 @RequestParam(value = "picture", required = false)MultipartFile picture) {
        return chatService.sendMessage(userId, groupId, text, picture);
    }


}
