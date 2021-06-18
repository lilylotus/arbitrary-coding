package cn.nihility.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JWTUtil {

    private static final String SECRET_KEY = "SECRET_KEY";
    private static final int EXPIRE_MILLIS_SECONDS = 60 * 60 * 1000;
    private static final Algorithm HMAC256 = Algorithm.HMAC256(SECRET_KEY);

    public static String createJwt(Map<String, String> params) {
        return createJwt(params, HMAC256);
    }

    public static String createJwt(Map<String, String> params, Algorithm algorithm) {

        String[] audience = {"app", "web"};
        JWTCreator.Builder builder = JWT.create();
        builder.withIssuer("auth0");  // 发布者
        builder.withAudience(audience); // 接收者
        builder.withNotBefore(new Date()); // 生效时间
        builder.withIssuedAt(new Date()); // 生成签名的时间
        builder.withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_MILLIS_SECONDS)); // token 有效时间
        builder.withJWTId(UUID.randomUUID().toString());
        params.forEach(builder::withClaim);

        return builder.sign(algorithm);
    }

    public static String createJwtRS256(Map<String, String> params) {
        Algorithm rsa256 = generateRS256();
        return createJwt(params, rsa256);
    }

    public static void verifierToken(String token) {
        verifierToken(token, HMAC256);
    }

    public static Algorithm generateRS256() {
        RSA256Key rsa256Key = null;
        try {
            rsa256Key = SecretKeyUtils.getRSA256Key();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert rsa256Key != null;
        return Algorithm.RSA256(rsa256Key.getPublicKey(), rsa256Key.getPrivateKey());
    }

    public static void verifierTokenRS256(String token) {
        Algorithm rsa256 = generateRS256();
        verifierToken(token, rsa256);
    }

    public static void verifierToken(String token, Algorithm algorithm) {
        verifierToken(token, algorithm, null);
    }

    public static boolean verifierToken(String token, Algorithm algorithm, Map<String, String> params) {
        Verification verification = JWT.require(algorithm).withIssuer("auth0");
        if (null != params && params.size() > 0) {
            params.forEach(verification::withClaim);
        }
        JWTVerifier verifier = verification.build();
        boolean ok;
        try {
            DecodedJWT verify = verifier.verify(token);
            ok = true;
            System.out.println(verify);

            String header = verify.getHeader();
            String payload = verify.getPayload();
            String signature = verify.getSignature();

            System.out.println("Header: " + header);
            System.out.println("payLoad: " + payload);
            System.out.println("signature: " + signature);

            System.out.println("Expire : " + verify.getExpiresAt());

            verify.getClaims().forEach((k, v) -> System.out.println(k + ":" + v.asString()));
        } catch (JWTVerificationException e) {
            System.out.println("inValid token");
            System.out.println(e.getMessage());
            ok = false;
        }
        return ok;
    }

    public static void main(String[] args) {
        Map<String, String> param = new HashMap<>();
        param.put("name", "小明");
        param.put("age", "20");

        String jwt = createJwt(param);
        System.out.println("jwt " + jwt);

        String tokenOk = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOlsiYXBwIiwid2ViIl0sIm5iZiI6MTYwMDA2NzY1NywiaXNzIjoiYXV0aDAiLCJuYW1lIjoi5bCP5piOIiwiZXhwIjoxNjAwMDcxMjU3LCJpYXQiOjE2MDAwNjc2NTcsImp0aSI6IjljNzMyM2Q0LTM1OGQtNGI1Ny1iOGIyLTIzOTVhOTBiMzcyMSIsImFnZSI6IjIwIn0.qzjnwJfj0-ODNjAjNKUuRoynpffUymJJL62dFFN-h9w";
        String tokenExpire = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOlsiYXBwIiwid2ViIl0sIm5iZiI6MTYwMDA2NzY1NywiaXNzIjoiYXV0aDAiLCJuYW1lIjoi5bCP5piOIiwiZXhwIjoxNjAwMDcxMjU3LCJpYXQiOjE2MDAwNjc2NTcsImp0aSI6IjljNzMyM2Q0LTM1OGQtNGI1Ny1iOGIyLTIzOTVhOTBiMzcyMSIsImFnZSI6IjIwIn0.qzjnwJfj0-ODNjAjNKUuRoynpffUymJJL62dFFN-h9w";
        verifierToken(jwt, HMAC256, param);


        System.out.println("============================");
        String jwtRS256 = createJwtRS256(param);
        System.out.println(jwtRS256);

        verifierTokenRS256(jwtRS256);
    }

}
