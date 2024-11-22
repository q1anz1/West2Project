package west2project.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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
    public Result<?> sendMessage(@RequestParam("session_id")Long sessionId,
                                 @RequestParam(value = "text", required = false)String text,
                                 @RequestParam(value = "picture", required = false)MultipartFile picture) throws Exception {
        return chatService.sendMessage(sessionId, text, picture);
    }

    @GetMapping("/session/list/get")
    public Result<?> getSessionList() {
        return chatService.getSessionList();
    }

    @PostMapping("/session/new")
    public Result<?> newSession(@RequestParam(value = "user_id", required = false)Long userId,
                                @RequestParam(value = "group_id", required = false)Long groupId) {
        return chatService.newSession(userId, groupId);
    }
}
