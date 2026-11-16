package com.example.wchat_app.utils;

import java.util.Date;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {

    // 密钥，实际应用中应该配置在属性文件中并加密
    private static final String SECRET_KEY = System.getenv("JWT_SECRET");

    // 使用新版本API创建密钥
    private static final Key key = createKey();
    private static Key createKey() {
        if (SECRET_KEY == null || SECRET_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) throw new IllegalStateException("Set JWT_SECRET with at least 32 bytes");
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // token有效期（毫秒），这里设置为24小时
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /**
     * 根据用户ID生成token
     */
    public String generateToken(Integer userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(key)  // 使用新版本的签名方法
                .compact();
    }

    /**
     * 从token中获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Integer.class);
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()  // 使用新版本的解析方法
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()  // 使用新版本的解析方法
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}