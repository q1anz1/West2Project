package west2project.filter;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import west2project.utils.JwtUtil;

import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {
    @Resource
    JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt=jwtUtil.getJwt(request);
        if(jwt != null){
            if(jwtUtil.verifyToken(jwt)){
                if(!jwtUtil.isInBlackList(jwt)){
                    UserDetails user=jwtUtil.toUser(Objects.requireNonNull(jwtUtil.getTokenClaims(jwt)));
                    UsernamePasswordAuthenticationToken authentication=
                            new UsernamePasswordAuthenticationToken(user,null, user.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request,response);
    }
}
