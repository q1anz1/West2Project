package west2project.service.impl;

import cn.hutool.core.date.DateTime;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import west2project.context.CommonContexts;
import west2project.context.RedisContexts;
import west2project.exception.ArgsInvalidException;
import west2project.mapper.UserMapper;
import west2project.pojo.DO.user.UserDO;
import west2project.pojo.DTO.user.RegisterDTO;
import west2project.pojo.VO.user.UserInfoVO;
import west2project.result.Result;
import west2project.service.UserService;
import west2project.util.*;

import java.util.Date;

import static west2project.context.CommonContexts.DEFAULT_AVATAR_URL;
import static west2project.context.RedisContexts.REDIS_EMAIL_CODE_TTL;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletRequest  httpServletRequest;
    private final EmailService emailService;
    @Override//注册
    public Result addUser(RegisterDTO registerDTO) {
        //验证是否有空值
        if (!SaveUtil.areAllFieldsNonNullOrEmpty(registerDTO)) {
            throw new ArgsInvalidException("有未填参数");
        }
        //验证邮箱是否合法
        String email = registerDTO.getEmail();
        if (TikaUtil.isEmailValid(email)) {
            throw new ArgsInvalidException("邮箱不合法");
        }
        //检测用户是否存在
        String username = registerDTO.getUsername();
        if (!(userMapper.findUserByUsername(username) == null)) {
            throw new ArgsInvalidException("用户名已存在");
        }
        if (!(userMapper.findUserIdByEmail(email) == null)) {
            throw new ArgsInvalidException("邮箱已被使用");
        }
        //验证邮箱验证码
        String registerCode = registerDTO.getCode();
        String code = RedisUtil.find(CommonContexts.EMAIL_CODE , email);
        if (!registerCode.equals(code)) {//如果code错误
            throw new ArgsInvalidException("邮箱验证码错误");
        }
        //名字过长处理
        if (username.length() > 32) {
            throw new ArgsInvalidException("名字过长");
        }
        //密码过长处理
        if (registerDTO.getPassword().length() > 32) {
            throw new ArgsInvalidException("密码过长");
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
        if(TikaUtil.isEmailValid(email)){
            throw new ArgsInvalidException("邮箱不合法");
        }
        try {
            String code = MyRandomUtil.randomCapitalLetter(6);
            RedisUtil.writeDataWithTTL(CommonContexts.EMAIL_CODE , email, code, REDIS_EMAIL_CODE_TTL);
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
            throw new ArgsInvalidException("验证码发送失败");
        }
    }

    @Override
    public Result getUserInfo(Long userId) {
        Result result = RedisUtil.findJsonWithCache(RedisContexts.CACHE_USER_INFO,userId, UserInfoVO.class,
                userMapper::findUserInfoVOByUserId, RedisContexts.CACHE_USER_INFO_TTL);
        if(result.getBase().getCode() != 1000){
            throw new ArgsInvalidException("未找到用户");
        }

        UserInfoVO user = (UserInfoVO) result.getData();
        user.setAvatarUrl(SaveUtil.imageToBase64(user.getAvatarUrl()));
        return Result.success(user);
    }

    @Override
    public Result logout() {
        String jwt = JwtUtil.getJwt(httpServletRequest);
        String jwtUUID= JwtUtil.getUUID(jwt);
        if (jwtUUID == null) {
            throw new ArgsInvalidException("未登入无法登出");
        }
        if(!JwtUtil.verifyToken(jwt)){
            throw new ArgsInvalidException("令牌错误");
        }
        JwtUtil.blackList(jwt);
        return Result.success();
    }

    @Override
    public Result updateAvatar(MultipartFile file) {
        String isValid = TikaUtil.isImageValid(file);
        if(!isValid.equals("true")){
            throw new ArgsInvalidException(isValid);
        }
        //保存图片到本地
        String imageUrl= SaveUtil.saveFileWithName(file, CommonContexts.DEFAULT_AVATAR_BOX, SaveUtil.changeFileName(file));
        //保存图片到数据库
        String jwt= JwtUtil.getJwt(httpServletRequest);
        Long id = JwtUtil.getUserId(jwt);
        userMapper.saveAvatar(id,imageUrl, DateTime.now());
        return Result.success();
    }
}
