package cn.nihility.jwt;

import io.jsonwebtoken.*;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    private static final String JWT_SECRET = "JWT_SECRET";
    private static final long DEFAULT_TTL_MILLIS = 120 * 60 * 1000L;

    private JwtTokenUtil() {
    }

    public static void main(String[] args) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userName", "testUserName");
        final String tokenTest = generateJwtToken("tokenTest", DEFAULT_TTL_MILLIS, claims);
        System.out.println(tokenTest);

        System.out.println(validateJwtToken(tokenTest));

        final Claims cs = validateJwtTokenWithClaims(tokenTest);
        if (null != cs) {
            cs.forEach((k, v) -> System.out.println("key [" + k + "] v [" + v + "]"));
        }
    }

    public static String generateJwtToken(String subject, long ttlMillis, Map<String, Object> claims) {
        final JwtBuilder jwtBuilder = Jwts.builder()
            .setSubject(subject) // 主题
            .setIssuer("jwt") // 签发者
            .setIssuedAt(new Date()) // 签发时间
            .signWith(SignatureAlgorithm.HS256, generalKey());

        if (null != claims && !claims.isEmpty()) {
            jwtBuilder.setClaims(claims);
        }
        jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + ttlMillis));

        return jwtBuilder.compact();
    }

    public static Claims validateJwtTokenWithClaims(final String jwtToken) {
        try {
            return parseJWT(jwtToken);
        } catch (ExpiredJwtException e) {
            log.error("Jwt Token 过期", e);
        } catch (SignatureException e) {
            log.error("Jwt Token 签名异常", e);
        } catch (Exception e) {
            log.error("Jwt Token 校验异常", e);
        }
        return null;
    }

    public static boolean validateJwtToken(final String jwtToken) {
        try {
            parseJWT(jwtToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Jwt Token 过期", e);
        } catch (SignatureException e) {
            log.error("Jwt Token 签名异常", e);
        } catch (Exception e) {
            log.error("Jwt Token 校验异常", e);
        }
        return false;
    }

    private static Claims parseJWT(String jwt) {
        return Jwts.parser().setSigningKey(generalKey())
            .parseClaimsJws(jwt)
            .getBody();
    }

    private static SecretKey generalKey() {
        byte[] encodedKey = Base64.decodeBase64(JWT_SECRET);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

}
