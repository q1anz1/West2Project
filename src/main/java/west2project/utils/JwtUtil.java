package west2project.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    public final RedisUtil redisUtil;
    public final West2RandomUtil west2RandomUtil;

    public final Long JWT_TTL = 24 * 60 * 60 * 1000L;//60*60*1000L为1小时
    public final String JWT_KEY = "Qianz";

    private final String secret = "~AMg)YBB=xx4UnlU=*OBX|+L@ys[hHJiv6Wkm_U`dV04+S55/x>g7GUXhpIR>Krx";
    private final  SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));



    public String createToken(String subject, Map<String, Object> claims) {
        try {
            return "bearer " + Jwts.builder()
                    .subject(subject)
                    .id(UUID.randomUUID().toString())
                    .issuer(JWT_KEY)
                    .issuedAt(new Date())
                    .claims(claims)
                    .expiration(new Date(System.currentTimeMillis() + JWT_TTL))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            return null;
        }
    }

    public Jws<Claims> parseClaim(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public Claims getTokenClaims(String token) {
        try {
            return parseClaim(token).getPayload();
        } catch (Exception e) {
            log.info("令牌解析错误:{}", token);
            return null;
        }
    }



    public boolean verifyToken(String token) {
        Claims claims = getTokenClaims(token);
        if (claims == null) {
            return false;
        }
        if (claims.getExpiration().before(new Date())) {
            return false;
        }
        return claims.getIssuer().equals(JWT_KEY);
    }
    public UserDetails toUser(Claims claims) {
        String authorities = claims.get("authorities").toString();
        return User.withUsername(String.valueOf(claims.get("username"))).password(String.valueOf(claims.get("password")))
                .authorities(authorities.substring(1,authorities.length()-1).split(","))
                .build();
    }

    public String getJwt(HttpServletRequest httpServletRequest) {
        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.toLowerCase().startsWith("bearer ")) {
            return null;
        }
        return authorizationHeader.substring("bearer ".length()).trim();
    }

    public Long getTime(String jwt) {//获取剩余有效时间
        Claims claims = getTokenClaims(jwt);
        if (claims != null) {
            long now = System.currentTimeMillis();
            long exp = (long) claims.get("exp");
            return now - exp;
        }
        return null;
    }
    public Long getUserId(String jwt) {
        Claims claims = getTokenClaims(jwt);
        if (claims != null) {
            return Long.parseLong(claims.get("sub").toString());
        }
        return null;
    }
    public Long getUserId(HttpServletRequest httpServletRequest) {
        Claims claims = getTokenClaims(getJwt(httpServletRequest));
        if (claims != null) {
            return Long.parseLong(claims.get("sub").toString());
        }
        return null;
    }

    public String getUUID(String jwt) {
        Claims claims = getTokenClaims(jwt);
        if (claims != null) {
            return claims.get("jti").toString();
        }
        return null;
    }
    public boolean isInBlackList(String jwt) {
        return redisUtil.find("jwt:blacklist:" , getUUID(jwt)) != null;
    }

    public void blackList(String jwt) {
        String jwtUUID = getUUID(jwt);
        redisUtil.writeDataWithTTL("jwt:blacklist:" , jwtUUID, "1", getTime(jwt));
    }
}
