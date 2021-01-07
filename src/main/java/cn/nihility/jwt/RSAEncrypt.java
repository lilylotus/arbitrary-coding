package cn.nihility.jwt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA 需要产生公钥和私钥，当采用公钥加密时，使用私钥解密；采用私钥加密时，使用公钥解密。
 */
public class RSAEncrypt {

    private static RSAKeyPair keyPair;

    static {
        try {
            keyPair = genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        String msg = "nihao 你好";

        final String encrypt = encrypt(msg);
        System.out.println("encrypt " + encrypt);

        final String decrypt = decrypt(encrypt);
        System.out.println("decrypt " + decrypt);

        System.out.println("public key encode " + keyPair.getPublicKeyEncode());
        System.out.println("private key encode " + keyPair.getPrivateKeyEncode());
    }

    public static String encrypt(String str) throws Exception {
        final byte[] decoded = Base64.decodeBase64(keyPair.getPublicKeyEncode());
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));

        // RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        return Base64.encodeBase64String(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String str) throws Exception {
        // 64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8));
        // base64编码的私钥
        byte[] decoded = Base64.decodeBase64(keyPair.getPrivateKeyEncode());
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        return new String(cipher.doFinal(inputByte), StandardCharsets.UTF_8);
    }

    /**
     * 随机生成密钥对
     */
    public static RSAKeyPair genKeyPair() throws NoSuchAlgorithmException {
        // KeyPairGenerator 类用于生成公钥和私钥对，基于 RSA 算法生成对象
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为 96-1024 位
        keyPairGenerator.initialize(1024);
        // 生成一个密钥对，保存在keyPair中
        final KeyPair keyPair = keyPairGenerator.genKeyPair();

        // 得到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        return encodeRSAKeyPair(privateKey, publicKey);
    }

    public static RSAKeyPair encodeRSAKeyPair(KeyPair keyPair) {
        // 得到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return encodeRSAKeyPair(privateKey, publicKey);
    }

    public static RSAKeyPair encodeRSAKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        final RSAKeyPair instance = new RSAKeyPair();
        instance.setPrivateKey(privateKey);
        instance.setPublicKey(publicKey);
        instance.setPrivateKeyEncode(new String(Base64.encodeBase64(privateKey.getEncoded())));
        instance.setPublicKeyEncode(new String(Base64.encodeBase64(publicKey.getEncoded())));
        return instance;
    }

    public static class RSAKeyPair {
        private String publicKeyEncode;
        private RSAPublicKey publicKey;
        private String privateKeyEncode;
        private RSAPrivateKey privateKey;

        public String getPublicKeyEncode() {
            return publicKeyEncode;
        }

        public void setPublicKeyEncode(String publicKeyEncode) {
            this.publicKeyEncode = publicKeyEncode;
        }

        public RSAPublicKey getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(RSAPublicKey publicKey) {
            this.publicKey = publicKey;
        }

        public String getPrivateKeyEncode() {
            return privateKeyEncode;
        }

        public void setPrivateKeyEncode(String privateKeyEncode) {
            this.privateKeyEncode = privateKeyEncode;
        }

        public RSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(RSAPrivateKey privateCrtKey) {
            this.privateKey = privateCrtKey;
        }

        @Override
        public String toString() {
            return "RSAKeyPair{" +
                    "publicKeyEncode='" + publicKeyEncode + '\'' +
                    ", privateKeyEncode='" + privateKeyEncode + '\'' +
                    '}';
        }
    }
}
