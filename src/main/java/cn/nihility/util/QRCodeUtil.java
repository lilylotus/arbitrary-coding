package cn.nihility.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author intel
 * @date 2021/09/30 11:40
 */
public class QRCodeUtil {


    private static final String DEFAULT_CHARSET = "utf-8";

    /**
     * 二维码默认空白区域大小
     */
    private static final Integer DEFAULT_MARGIN = 1;

    /**
     * 生成二维码图片
     *
     * @param content 二维码内容
     * @param width   二维码宽度
     * @param height  二维码高度
     * @param margin  二维码空白区域大小
     * @return BufferedImage
     * @throws Exception 异常
     */
    public static BufferedImage createQrCodeImage(String content, Integer width, Integer height, Integer margin) throws Exception {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("The width and height of the QR code must be greater than zero");
        }

        //二维码属性设置
        Map<EncodeHintType, Object> hints = new HashMap<>(4);
        //纠错等级
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, DEFAULT_CHARSET);
        //设置二维码空白区域大小
        hints.put(EncodeHintType.MARGIN, Optional.of(margin).orElse(DEFAULT_MARGIN));
        //生成比特矩阵
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        //构造BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        //填充图片像素值
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    public static BufferedImage createQrCodeImage(String content, Integer width, Integer height) throws Exception {
        return createQrCodeImage(content, width, height, DEFAULT_MARGIN);
    }

    public static void main(String[] args) throws Exception {
        final BufferedImage bi = createQrCodeImage("你好", 256, 256, DEFAULT_MARGIN);
        ImageIO.write(bi, "png", new FileOutputStream(new File("D:/qr.png")));
    }

}
