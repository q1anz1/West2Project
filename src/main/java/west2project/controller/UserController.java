package west2project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import west2project.pojo.DTO.user.RegisterDTO;
import west2project.result.Result;
import west2project.service.UserService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/user/a")
    public Result a() {
        return Result.error("成功哟Controller");
    }

    @PostMapping("/user/register")
    public Result register(@RequestParam("username") String username,@RequestParam("password") String password,
                           @RequestParam("email") String email,@RequestParam("code") String code) {
        RegisterDTO registerDTO = new RegisterDTO(username,password,email,code);
        log.info("用户注册，用户名:{} 。", registerDTO.getUsername());
        return userService.addUser(registerDTO);
    }

    @GetMapping("/auth/get-code")
    public Result getCode(@RequestParam String email) {
        log.info("发送邮箱验证码到邮箱:{} 。", email);
        return userService.getCode(email);
    }

    @GetMapping("user/info/{user_id}")
    public Result getUserInfo(@PathVariable("user_id") Long userId) {
        log.info("查询用户信息：{} 。", userId);
        return userService.getUserInfo(userId);
    }

    @GetMapping("/user/logout")
    public Result logout() {
        log.info("用户登出。");
        return userService.logout();
    }

    @PutMapping("/user/avatar/upload")
    public Result updateAvatar(@RequestParam("data") MultipartFile file){
        log.info("用户上传图片。");
        return userService.updateAvatar(file);
    }
}
