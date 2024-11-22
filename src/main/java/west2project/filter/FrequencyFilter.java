package west2project.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import west2project.result.Result;
import west2project.util.RedisUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static west2project.context.RedisContexts.REDIS_FREQUENCY;
import static west2project.enums.ResponseCodeEnum.CODE_666;

@Component
@RequiredArgsConstructor
public class FrequencyFilter extends OncePerRequestFilter {
    private static final Integer MAX_FREQUENCY = 100;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        RedisUtil.incrWithExpire(REDIS_FREQUENCY, ip, 10L, TimeUnit.SECONDS);
        // 频率控制
        int frequency = Integer.parseInt(RedisUtil.find(REDIS_FREQUENCY, ip));
        if (frequency >= MAX_FREQUENCY) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(Result.error(CODE_666.getCode(), CODE_666.getInfo()).asJsonString());
        } else {
            filterChain.doFilter(request,response);
        }
    }
}
