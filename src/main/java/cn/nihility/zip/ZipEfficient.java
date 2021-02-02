package cn.nihility.zip;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 高效 zip 压缩
 */
public class ZipEfficient {

    private static final String ZIP_FILE_DIR = "C:\\Users\\intel\\Desktop\\";
    private static final String ZIP_FILE = "picture.zip";
    private static final String ZIP_PIC_FILE = "C:\\Users\\intel\\Desktop\\20210202\\picture\\p2.jpg";

    public static void main(String[] args) {
        // 7338 ms
        zipFileNoBuffer();
        // 375 ms
        zipFileBuffer();
        // 300 ms
        zipFileChannel();
        // 299
        zipFileMap();
        // 474
        zipFilePip();
    }

    public static void zipFileNoBuffer() {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(ZIP_FILE_DIR + "nobuffer_" + ZIP_FILE)))) {
            //开始时间
            long beginTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                try (InputStream input = new FileInputStream(ZIP_PIC_FILE)) {
                    zipOut.putNextEntry(new ZipEntry("picture_" + i + ".jpg"));
                    int temp;
                    while ((temp = input.read()) != -1) {
                        zipOut.write(temp);
                    }
                }
            }
            System.out.println("zipFileNoBuffer duration time [" + (System.currentTimeMillis() - beginTime) + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 7338 ms
    }


    public static void zipFileBuffer() {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(ZIP_FILE_DIR + "buffer" + ZIP_FILE)));
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(zipOut)) {
            //开始时间
            long beginTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(ZIP_PIC_FILE))) {
                    zipOut.putNextEntry(new ZipEntry("picture_" + i + ".jpg"));
                    int temp;
                    while ((temp = bufferedInputStream.read()) != -1) {
                        bufferedOutputStream.write(temp);
                    }
                }
                //bufferedOutputStream.flush();
            }
            //bufferedOutputStream.flush();
            System.out.println("zipFileBuffer duration time [" + (System.currentTimeMillis() - beginTime) + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void zipFileChannel() {
        //开始时间
        long beginTime = System.currentTimeMillis();
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(ZIP_FILE_DIR + "channel_" + ZIP_FILE)));
             WritableByteChannel writableByteChannel = Channels.newChannel(zipOut)) {
            for (int i = 0; i < 10; i++) {
                final File picFile = new File(ZIP_PIC_FILE);
                try (FileChannel fileChannel = new FileInputStream(picFile).getChannel()) {
                    zipOut.putNextEntry(new ZipEntry("picture_" + i + ".jpg"));
                    fileChannel.transferTo(0, picFile.length(), writableByteChannel);
                }
            }
            System.out.println("zipFileChannel duration time [" + (System.currentTimeMillis() - beginTime) + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void zipFileMap() {
        //开始时间
        long beginTime = System.currentTimeMillis();
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(ZIP_FILE_DIR + "map_" + ZIP_FILE)));
             WritableByteChannel writableByteChannel = Channels.newChannel(zipOut)) {
            for (int i = 0; i < 10; i++) {

                zipOut.putNextEntry(new ZipEntry("picture_" + i + ".jpg"));

                final File picFile = new File(ZIP_PIC_FILE);
                //内存中的映射文件
                MappedByteBuffer mappedByteBuffer = new RandomAccessFile(picFile, "r").getChannel()
                    .map(FileChannel.MapMode.READ_ONLY, 0, picFile.length());

                writableByteChannel.write(mappedByteBuffer);
            }
            System.out.println("zipFileMap duration time [" + (System.currentTimeMillis() - beginTime) + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Version 5 使用Pip
    public static void zipFilePip() {

        long beginTime = System.currentTimeMillis();
        try(WritableByteChannel out = Channels.newChannel(new FileOutputStream(new File(ZIP_FILE_DIR + "pip_" + ZIP_FILE)))) {
            final Pipe pipe = Pipe.open();
            final File picFile = new File(ZIP_PIC_FILE);

            //异步任务
            CompletableFuture.runAsync(()->runTask(pipe, picFile));

            //获取读通道
            ReadableByteChannel readableByteChannel = pipe.source();
            ByteBuffer buffer = ByteBuffer.allocate(((int) picFile.length()) * 10);
            while (readableByteChannel.read(buffer) >= 0) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("zipFilePip duration time [" + (System.currentTimeMillis() - beginTime) + "]");

    }

    //异步任务
    public static void runTask(Pipe pipe, final File picFile) {
        try (ZipOutputStream zos = new ZipOutputStream(Channels.newOutputStream(pipe.sink()));
             WritableByteChannel out = Channels.newChannel(zos)) {
            for (int i = 0; i < 10; i++) {
                zos.putNextEntry(new ZipEntry("picture_" + i + ".jpg"));

                FileChannel jpgChannel = new FileInputStream(picFile).getChannel();

                jpgChannel.transferTo(0, picFile.length(), out);

                jpgChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
