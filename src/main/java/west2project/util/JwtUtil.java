package west2project.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    public static final Long JWT_TTL = 24 * 60 * 60 * 1000L;//60*60*1000L为1小时
    public static final String JWT_KEY = "Qianz";
    private static final String secret = "~AMg)YBB=xx4UnlU=*OBX|+L@ys[hHJiv6Wkm_U`dV04+S55/x>g7GUXhpIR>Krx";
    private static final  SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    public static String createToken(String subject, Map<String, Object> claims) {
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
            throw  new RuntimeException(e);
        }
    }

    public static Jws<Claims> parseClaim(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public static Claims getTokenClaims(String token) {
        try {
            return parseClaim(token).getPayload();
        } catch (Exception e) {
            log.error("令牌解析错误:{}", token);
            return null;
        }
    }

    public static boolean verifyToken(String token) {
        Claims claims = getTokenClaims(token);
        if (claims == null) {
            return false;
        }
        if (claims.getExpiration().before(new Date())) {
            return false;
        }
        return claims.getIssuer().equals(JWT_KEY);
    }

    public static UserDetails toUser(Claims claims) {
        String authorities = claims.get("authorities").toString();
        return User.withUsername(String.valueOf(claims.get("username"))).password(String.valueOf(claims.get("password")))
                .authorities(authorities.substring(1,authorities.length()-1).split(","))
                .build();
    }

    public static String getJwt(HttpServletRequest httpServletRequest) {
        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.toLowerCase().startsWith("bearer ")) {
            return null;
        }
        return authorizationHeader.substring("bearer ".length()).trim();
    }

    public static Long getTime(String jwt) {//获取剩余有效时间
        Claims claims = getTokenClaims(jwt);
        if (claims != null) {
            long now = System.currentTimeMillis();
            long exp = (long) claims.get("exp");
            return now - exp;
        }
        return null;
    }
    public static Long getUserId(String jwt) {
        Claims claims = getTokenClaims(jwt);
        if (claims != null) {
            return Long.parseLong(claims.get("sub").toString());
        }
        return null;
    }
    public static Long getUserId(HttpServletRequest httpServletRequest) {
        Claims claims = getTokenClaims(getJwt(httpServletRequest));
        if (claims != null) {
            return Long.parseLong(claims.get("sub").toString());
        }
        return null;
    }

    public static String getUUID(String jwt) {
        Claims claims = getTokenClaims(jwt);
        if (claims != null) {
            return claims.get("jti").toString();
        }
        return null;
    }
    public static boolean isInBlackList(String jwt) {
        return RedisUtil.find("jwt:blacklist:" , Objects.requireNonNull(getUUID(jwt))) != null;
    }

    public static void blackList(String jwt) {
        String jwtUUID = getUUID(jwt);
        if (jwtUUID != null) {
            RedisUtil.writeDataWithTTL("jwt:blacklist:" , jwtUUID, "1", getTime(jwt));
        }
    }
}
