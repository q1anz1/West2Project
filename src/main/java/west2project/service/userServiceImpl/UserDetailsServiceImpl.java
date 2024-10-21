package west2project.service.userServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import west2project.mapper.UserMapper;
import west2project.pojo.DO.users.UserDO;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据用户名查询用户信息
        UserDO user=userMapper.findUserByUsername(username);
        //若查询不到
        if(user==null){
            throw new RuntimeException("用户名或密码错误");
        }
        //若为封禁
        if(user.getDeletedAt() != null){
            throw new RuntimeException("该用户已被封禁");
        }
        return User.withUsername(username).password(user.getPassword()).roles(user.getRole()).build();
    }

}
