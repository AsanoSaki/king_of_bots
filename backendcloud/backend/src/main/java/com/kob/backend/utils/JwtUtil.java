package com.kob.backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    public static final long JWT_TTL = 60 * 60 * 1000L * 24 * 14;  // token的有效期设置为14天
    public static final String JWT_KEY = "IVK157AXCZSChcwW23AUvayrXYhgcXAHKBMDziw17dW";  // 密钥，自己随便打，但是长度要够长，否则会报错

    public static String getUUID() {  // 生成一个随机的UUID并去掉其中的"-"
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static SecretKey generalKey() {
        byte[] encodeKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);  // 使用Base64解码预设的JWT_KEY
        return new SecretKeySpec(encodeKey, 0, encodeKey.length, "HmacSHA256");  // 使用这个解码后的密钥生成一个HmacSHA256算法的SecretKey
    }

    private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis, String uuid) {
        SecretKey secretKey = generalKey();

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if (ttlMillis == null) {
            ttlMillis = JwtUtil.JWT_TTL;
        }
        long expMillis = nowMillis + ttlMillis;  // 计算出token的过期时间
        Date expDate = new Date(expMillis);

        return Jwts.builder()
                .id(uuid)
                .subject(subject)
                .issuer("sg")
                .issuedAt(now)
                .signWith(secretKey)
                .expiration(expDate);
    }

    public static String createJWT(String subject) {  // 创建一个JWT。
        JwtBuilder builder = getJwtBuilder(subject, null, getUUID());
        return builder.compact();  // 将其转换为一个紧凑的URL安全的JWT字符串
    }

    public static Claims parseJWT(String jwt) throws Exception {  // 解析一个JWT
        SecretKey secretKey = generalKey();
        return Jwts.parser()
                .verifyWith(secretKey)  // 使用生成的密钥来验证JWT的签名
                .build()
                .parseSignedClaims(jwt)
                .getPayload();  // 解析JWT并返回其payload（载荷）部分
    }
}
