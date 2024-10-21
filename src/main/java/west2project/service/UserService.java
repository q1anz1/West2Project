package west2project.service;

import org.springframework.web.multipart.MultipartFile;
import west2project.pojo.DTO.users.RegisterDTO;
import west2project.result.Result;

public interface UserService {
    Result addUser(RegisterDTO registerDTO);

    Result getCode(String email);

    Result getUserInfo(Long userId);

    Result logout();

    Result updateAvatar(MultipartFile file);
}
