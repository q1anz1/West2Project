package west2project.chat.service;

import org.springframework.web.multipart.MultipartFile;
import west2project.result.Result;

public interface ChatService {
    Result<?> sendMessage(Long userId, Long groupId, String text, MultipartFile picture);
}
