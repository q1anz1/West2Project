package west2project.service.userServiceImpl;

import cn.hutool.core.date.DateTime;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import west2project.context.Contexts;
import west2project.mapper.UserMapper;
import west2project.pojo.DO.users.UserDO;
import west2project.pojo.DTO.users.RegisterDTO;
import west2project.pojo.VO.users.UserInfoVO;
import west2project.result.Result;
import west2project.service.UserService;
import west2project.utils.*;

import java.util.Date;

import static west2project.context.Contexts.DEFAULT_AVATAR_URL;
import static west2project.context.Contexts.REDIS_EMAIL_CODE_TTL;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final West2RandomUtil west2RandomUtil;
    private final SaveUtil saveUtil;
    private final HttpServletRequest  httpServletRequest;
    private final JwtUtil jwtUtil;
    private final TikaUtil tikaUtil;
    private final EmailService emailService;
    @Override//注册
    public Result addUser(RegisterDTO registerDTO) {
        //验证是否有空值
        if (!saveUtil.areAllFieldsNonNullOrEmpty(registerDTO)) {
            return Result.error("有未设置参数");
        }
        //验证邮箱是否合法
        String email = registerDTO.getEmail();
        if (tikaUtil.isEmailValid(email)) {
            return Result.error("邮箱不合法");
        }
        //检测用户是否存在
        String username = registerDTO.getUsername();
        if (!(userMapper.findUserByUsername(username) == null)) {
            return Result.error("用户名已存在");
        }
        if (!(userMapper.findUserIdByEmail(email) == null)) {
            return Result.error("邮箱已被使用");
        }
        //验证邮箱验证码
        String registerCode = registerDTO.getCode();
        String code = redisUtil.find(Contexts.EMAIL_CODE , email);
        if (!registerCode.equals(code)) {//如果code错误
            return Result.error("邮箱验证码错误");
        }
        //名字过长处理
        if (username.length() > 32) {
            return Result.error("名字过长");
        }
        //密码过长处理
        if (registerDTO.getPassword().length() > 32) {
            return Result.error("密码过长");
        }
        //将DTO转化为DO
        UserDO userDO = new UserDO();
        userDO.setUsername(username);
        userDO.setEmail(email);
        userDO.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        userDO.setAvatarUrl(DEFAULT_AVATAR_URL);
        userDO.setCreatedAt(new Date(System.currentTimeMillis()));
        userDO.setUpdatedAt(new Date(System.currentTimeMillis()));
        //插入数据库
        userMapper.saveUser(userDO.getUsername(), userDO.getPassword(), userDO.getAvatarUrl(), userDO.getCreatedAt(),
                userDO.getUpdatedAt(), email);
        return Result.success();
    }

    @Override//发送邮箱验证码
    public Result getCode(String email) {
        if(tikaUtil.isEmailValid(email)){
            return Result.error("邮箱不合法");
        }
        try {
            String code = west2RandomUtil.randomCapitalLetter(6);
            redisUtil.writeDataWithTTL(Contexts.EMAIL_CODE , email, code, REDIS_EMAIL_CODE_TTL);
            // 用邮箱发送验证码
            Thread thread = new Thread(() -> {
                try {
                    emailService.sendEmailCode(email,code);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
            log.info("验证码为：{}", code);
            return Result.success();
        } catch (Exception e) {
            return Result.error("验证码发送失败");
        }
    }

    @Override
    public Result getUserInfo(Long userId) {
        Result result = redisUtil.findJsonWithCache(Contexts.CACHE_USERDO,userId, UserInfoVO.class,
                userMapper::findUserInfoVOByUserId,Contexts.CACHE_USERDO_TTL);
        if(result.getBase().getCode() != 1000){
            return Result.error("未找到用户");
        }

        UserInfoVO user = (UserInfoVO) result.getData();
        user.setAvatarUrl(saveUtil.imageToBase64(user.getAvatarUrl()));
        return Result.success(user);
    }

    @Override
    public Result logout() {
        String jwt = jwtUtil.getJwt(httpServletRequest);
        String jwtUUID= jwtUtil.getUUID(jwt);
        if (jwtUUID == null) {
            return Result.error("未登入无法登出呢");
        }
        if(!jwtUtil.verifyToken(jwt)){
            return Result.error("令牌错误");
        }
        jwtUtil.blackList(jwt);
        return Result.success();
    }

    @Override
    public Result updateAvatar(MultipartFile file) {
        String isValid = tikaUtil.isImageValid(file);
        if(!isValid.equals("true")){
            return Result.error(isValid);
        }
        //保存图片到本地
        String imageUrl= saveUtil.saveFileWithName(file, Contexts.DEFAULT_AVATAR_BOX, saveUtil.changeFileName(file));
        //保存图片到数据库
        String jwt=jwtUtil.getJwt(httpServletRequest);
        Long id = jwtUtil.getUserId(jwt);
        userMapper.saveAvatar(id,imageUrl, DateTime.now());
        return Result.success();
    }
}
