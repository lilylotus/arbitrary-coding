package cn.nihility.util;

import cn.nihility.exception.ZipOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * ZIP 文件解压工具类
 */
public class ZipUtil {

    private static final Logger log = LoggerFactory.getLogger(ZipUtil.class);

    private static final int BUFFER_SIZE = 2048;

    private ZipUtil() {
    }

    /**
     * 压缩文件夹为 zip
     *
     * @param zipDirPath       压缩文件夹路径
     * @param zipFilePath      压缩文件路径
     * @param keepDirStructure 是否保留原来的目录结构
     *                         true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws ZipOperationException 压缩失败会抛出异常
     */
    public static void zipDirResources(final String zipDirPath, final String zipFilePath, boolean keepDirStructure)
        throws ZipOperationException {
        try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath), StandardCharsets.UTF_8)) {
            File sourceDirFile = new File(zipDirPath);
            compress(sourceDirFile, zos, sourceDirFile.getName(), keepDirStructure);
        } catch (Exception e) {
            log.error("压缩文件夹 [{}] 中内容异常", zipDirPath);
            throw new ZipOperationException("压缩文件夹 [" + zipDirPath + "] 异常", e);
        }
    }

    /**
     * 把文件列表压缩为 zip
     *
     * @param srcFiles    需要压缩的文件列表
     * @param zipFilePath 压缩文件路径
     * @throws ZipOperationException 压缩失败会抛出异常
     */
    public static void zipFileList(List<File> srcFiles, final String zipFilePath) throws ZipOperationException {
        try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            int len;
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[BUFFER_SIZE];
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                try (final FileInputStream is = new FileInputStream(srcFile)) {
                    while ((len = is.read(buf)) != -1) {
                        zos.write(buf, 0, len);
                    }
                }
                zos.closeEntry();
            }
        } catch (Exception e) {
            log.error("ZIP 压缩文件列表异常");
            throw new ZipOperationException("ZIP 压缩文件列表异常", e);
        }
    }

    /**
     * 递归压缩方法
     *
     * @param sourceFile       源文件
     * @param zos              zip输出流
     * @param name             压缩后的名称
     * @param keepDirStructure 是否保留原来的目录结构
     *                         true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean keepDirStructure) throws Exception {
        if (sourceFile.isFile()) {
            // 向 zip 输出流中添加一个 zip 实体，构造器中 name 为 zip 实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy 文件到 zip 输出流中
            byte[] buf = new byte[2048];
            int len;
            try (final FileInputStream is = new FileInputStream(sourceFile)) {
                while ((len = is.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
            }
            // Complete the entry
            zos.closeEntry();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (keepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的 copy
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (keepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(), keepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), keepDirStructure);
                    }
                }
            }
        }
    }

    /**
     * zip 解压指定文件内容
     *
     * @param zipFile        zip源文件
     * @param suffixFileName 要解压在 ZIP 文件的指定文件，注意：解压指定文件夹中的文件，文件夹分隔符为 / (反斜线）
     * @throws ZipOperationException 解压失败会抛出运行时异常
     */
    public static String unZipTargetFile(final File zipFile, String suffixFileName) throws ZipOperationException {
        final String zipFileAbsolutePath = zipFile.getAbsolutePath();
        final StringBuilder targetFileContent = new StringBuilder(64);
        // 判断源 ZIP 文件是否存在
        if (!zipFile.exists()) {
            throw new ZipOperationException("ZIP 文件 [" + zipFileAbsolutePath + "] 不存在");
        }
        // 开始解压
        try (final ZipFile unzipFile = new ZipFile(zipFile)) {
            Enumeration<?> entries = unzipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                final String entryName = entry.getName();
                // 当 ZIP 文件中的文件名称和指定要解压的文件名称对应，获取该文件内容
                if (!entry.isDirectory() && entryName.endsWith(suffixFileName)) {
                    // 将压缩文件内容提取出来
                    try (final InputStream is = unzipFile.getInputStream(entry);
                         final ByteArrayOutputStream bos = new ByteArrayOutputStream(128)) {
                        int len;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while ((len = is.read(buffer)) != -1) {
                            bos.write(buffer, 0, len);
                        }
                        targetFileContent.append(new String(bos.toByteArray(), StandardCharsets.UTF_8));
                    }
                    break;
                }
            }
        } catch (IOException e) {
            log.error("解压 ZIP 文件 [{}] 中的指定文件 [{}] 异常", zipFileAbsolutePath, suffixFileName);
            throw new ZipOperationException("解压 ZIP 文件 [" + zipFile.getName() + "] 中的指定文件 [" + suffixFileName + "] 异常", e);
        }

        return targetFileContent.toString();
    }

    /**
     * zip解压
     *
     * @param zipFile      zip源文件
     * @param unzipDirPath 解压后的目标文件夹
     * @throws ZipOperationException 解压失败会抛出运行时异常
     */
    public static void unZip(final File zipFile, String unzipDirPath) throws ZipOperationException {
        long start = System.currentTimeMillis();
        final String zipFileAbsolutePath = zipFile.getAbsolutePath();
        // 判断源 ZIP 文件是否存在
        if (!zipFile.exists()) {
            throw new ZipOperationException("ZIP 文件 [" + zipFileAbsolutePath + "] 不存在");
        }
        // 开始解压
        try (final ZipFile unzipFile = new ZipFile(zipFile)) {
            Enumeration<?> entries = unzipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                // 在 ZIP 包中的文件解压对应的本地目录中的文件
                File entryMapLocalFile = new File(unzipDirPath + File.separator + zipEntry.getName());
                // 如果是文件夹，就创建个文件夹
                if (zipEntry.isDirectory()) {
                    if (!entryMapLocalFile.exists() && !entryMapLocalFile.mkdirs()) {
                        log.warn("创建目录 [{}] 失败", entryMapLocalFile.getAbsolutePath());
                    }
                } else {
                    // 如果是文件，就先创建一个文件，然后用 io 流把内容 copy 过去
                    // 保证这个文件的父文件夹必须要存在
                    if (!entryMapLocalFile.getParentFile().exists() && !entryMapLocalFile.getParentFile().mkdirs()) {
                        log.warn("创建父文件夹 [{}] 失败", entryMapLocalFile.getParentFile().getAbsolutePath());
                    }
                    if (!entryMapLocalFile.exists() && !entryMapLocalFile.createNewFile()) {
                        log.warn("创建文件 [{}] 失败", entryMapLocalFile.getAbsolutePath());
                    }
                    // 将压缩文件内容写入到这个文件中
                    try (final InputStream is = unzipFile.getInputStream(zipEntry);
                         final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(entryMapLocalFile, false))) {
                        int len;
                        byte[] buffer = new byte[BUFFER_SIZE];
                        while ((len = is.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.flush();
                        // 关流顺序，先打开的后关闭
                    }
                }
            }
        } catch (IOException e) {
            log.error("解压 ZIP 文件 [{}] 到目录 [{}] 异常", zipFileAbsolutePath, unzipDirPath);
            throw new ZipOperationException("解压 ZIP 文件 [" + zipFile.getName() + "] 到目录 [" + unzipDirPath + "] 异常", e);
        }

        log.info("解压 ZIP 文件 [{}] 完成，耗时 [{}] ms", zipFileAbsolutePath, (System.currentTimeMillis() - start));

    }

}
