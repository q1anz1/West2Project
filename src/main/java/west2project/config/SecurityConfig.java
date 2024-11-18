package west2project.config;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import west2project.filter.JwtAuthorizeFilter;
import west2project.mapper.UserMapper;
import west2project.pojo.VO.user.LoginVO;
import west2project.result.Result;
import west2project.util.JwtUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {
    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;
    @Resource
    UserMapper userMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/user/login","user/register","/auth/get-code").permitAll()
                                .requestMatchers("/admin/**").hasRole("admin")
                                .anyRequest().hasAnyRole("admin","user")
                          )
                .formLogin(conf ->{//配置表单登入
                    conf.loginPage("/user/login")//配置登入地址
                    .failureHandler(this::onAuthenticationFailure)
                    .successHandler(this::onAuthenticationSuccess);
                })
                .logout(conf ->{//配置登出时相关
                    conf.logoutUrl("/Logout")//登出url
                            .logoutSuccessHandler(this::onLogoutSuccess);
                })
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::authenticationEntryPoint)
                        .accessDeniedHandler(this::onAccessDeny))
                .rememberMe(conf -> conf
                        .rememberMeParameter("remember-me"))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        User user = (User) authentication.getPrincipal();//这个user是spring的user
        // 获取jwt令牌
        Map<String,Object> map=new HashMap<>();
        map.put("username",user.getUsername());
        map.put("authorities",user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        String jwt= JwtUtil.createToken(userMapper.findUserIdByUsername(user.getUsername()).toString(),map);
        // 返回前端
        LoginVO loginVO = new LoginVO();
        loginVO.setJwtToken(jwt);
        response.getWriter().write(Result.success(loginVO).asJsonString());
    }

    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(Result.error("账号或密码错误").asJsonString());
    }

    public void authenticationEntryPoint(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(Result.error("未登入").asJsonString());
    }

    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    }

    public void onAccessDeny(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(Result.error("无权限").asJsonString());
    }

}
