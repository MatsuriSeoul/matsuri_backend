package side.side.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;


@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.expiration.ms}")
    private long expirationMs; // 토큰 만료 시간

    @Value("${jwt.secret}")
    private String SECRET_KEY; // 비밀 키

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(String userName, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userName", userName)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUserNameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();
            return claims.get("userName", String.class);
        } catch (Exception e) {
            log.error("토큰에서 사용자 닉네임 추출 에러: {}", e.getMessage());
            return null;
        }
    }

    public Long extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();
            return claims.get("userId", Long.class); // userId를 claim에서 추출
        } catch (Exception e) {
            log.error("토큰에서 사용자 ID 추출 에러: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            token = token.replace("Bearer ", "");
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("유효하지 않은 JWT 토큰: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("만료된 JWT 토큰: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("지원되지 않는 JWT 토큰: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT 클레임 문자열이 비어 있음: {}", ex.getMessage());
        }
        return false;
    }
}
