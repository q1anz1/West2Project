package west2project.chat.service;

import org.springframework.web.multipart.MultipartFile;
import west2project.result.Result;

public interface ChatService {
    Result<?> sendMessage(Long sessionId, String text, MultipartFile picture) throws Exception;

    Result<?> getSessionList();

    Result<?> newSession(Long userId, Long groupId);

    Result<?> getMessage(Long userId, Long groupId);

    Result<?> newGroup(String name, String text, MultipartFile multipartFile);

    Result<?> getFriend();

    Result<?> getGroup();

    Result<?> joinGroup(Long groupId);
}
