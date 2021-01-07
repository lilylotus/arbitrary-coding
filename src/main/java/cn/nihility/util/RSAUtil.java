package cn.nihility.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtil {

    public static void main(String[] args) throws Exception {
        String publicKeyFilePath = "D:\\id_rsa.pub";
        PublicKey publicKey = getPublicKey(publicKeyFilePath);
        System.out.println(publicKey);
    }

    /**
     * 从文件中读取公钥
     * @param publicKeyFilePath 公钥文件路径
     * @return 公钥
     */
    public static PublicKey getPublicKey(String publicKeyFilePath) throws Exception {
        byte[] bytes = readFile(publicKeyFilePath);
        return parsePublicKey(bytes);
    }

    /**
     * 解析公钥
     * @param bytes 公钥数据流
     * @return 解析完成后的公钥
     */
    private static PublicKey parsePublicKey(byte[] bytes) throws Exception {
        byte[] decode = Base64.getDecoder().decode(bytes);
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(decode);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(encodedKeySpec);
    }

    /**
     * 读取文件为字节数组
     * @param filePath 文件路径
     * @return 文件的字节
     */
    private static byte[] readFile(String filePath) throws IOException {
        return Files.readAllBytes(new File(filePath).toPath());
        /*ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(filePath)))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        }
        return outputStream.toByteArray();*/
    }

}
