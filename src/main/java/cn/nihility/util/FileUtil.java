package cn.nihility.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    /**
     * 系统适配的文件内容换行符， windows -> \n\r , linux -> \r
     */
    public static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();

    private static final String BACKEND_PATH = "/backend";

    private FileUtil() {
    }

    /**
     * 获取项目启动时的目录
     *
     * @return 项目启动时的目录
     */
    public static String projectRootDir() {
        URL resource = FileUtil.class.getClassLoader().getResource("");
        String rootPath;
        if (null != resource) {
            String path = resource.getPath();
            if (path.lastIndexOf("/lib") != -1) {
                path = path.substring(0, path.lastIndexOf("/lib"));
            } else if (path.lastIndexOf(BACKEND_PATH) != -1) {
                path = path.substring(0, path.lastIndexOf(BACKEND_PATH) + BACKEND_PATH.length());
            }
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                path = path.substring(1);
            }
            rootPath = path;
        } else {
            rootPath = Paths.get("").toFile().getAbsolutePath();
            rootPath = rootPath.replace("\\", "/");
        }
        if (rootPath.endsWith("/")) {
            rootPath = rootPath.substring(0, rootPath.length() - 1);
        }
        return rootPath;
    }

    /**
     * 往指定文件中以覆盖的方式写入内容
     *
     * @param content   要写入的文件内容
     * @param writeFile 要覆盖写入的文件
     * @throws IOException 写入文件失败
     */
    public static void writeContentOverWriteFile(final String content, File writeFile) throws IOException {
        if (StringUtils.isBlank(content) || null == writeFile) {
            return;
        }
        try {
            createNewFile(writeFile);
            Files.write(writeFile.toPath(), content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("覆盖方式写入文件 [{}] 内容失败", writeFile.getAbsolutePath());
            throw new IOException("覆盖方式写入文件 [" + writeFile.getName() + "] 内容失败", e);
        }
    }

    /**
     * 创建一个一的文件
     */
    public static void createNewFile(File file) throws IOException {
        if (null != file && !file.exists()) {
            String msg = (file.createNewFile() ? "成功" : "失败");
            log.info("创建文件 [{}] - [{}]", file.getAbsolutePath(), msg);
        }
    }

    /**
     * 往指定文件中以覆盖的方式写入内容
     *
     * @param content   要写入的文件内容
     * @param writeFile 要覆盖写入的文件
     * @throws IOException 写入文件失败
     */
    public static void writeContentAppendFile(final String content, File writeFile) throws IOException {
        if (StringUtils.isBlank(content) || null == writeFile) {
            return;
        }
        try {
            createNewFile(writeFile);
            Files.write(writeFile.toPath(), content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("追加方式写入文件 [{}] 内容失败", writeFile.getAbsolutePath());
            throw new IOException("追加方式写入文件 [" + writeFile.getName() + "] 内容失败", e);
        }
    }

    /**
     * 创建目录或多级目录
     *
     * @param dir 要创建的目录
     * @return true -> 创建成功
     */
    public static boolean mkdirs(String dir) {
        if (StringUtils.isNotBlank(dir)) {
            final File dirFile = new File(dir);
            if (!dirFile.exists() && !dirFile.mkdirs()) {
                log.error("创建目录 [{}] 失败", dir);
                return false;
            }
        }
        return true;
    }

    /**
     * 级联探索指定目录中所有的 JAR 文件
     *
     * @param rootFile    指定获取所有文件的目录
     * @param jarFileList 保存递归遍历到的 jar 类型文件
     */
    public static void exploreDirCascadeJarFile(File rootFile, List<File> jarFileList) {
        if (!rootFile.exists()) {
            return;
        }
        if (rootFile.isDirectory()) {
            final File[] childFiles = rootFile.listFiles();
            if (null != childFiles && childFiles.length > 0) {
                for (File f : childFiles) {
                    exploreDirCascadeJarFile(f, jarFileList);
                }
            }
        } else if (rootFile.isFile() && rootFile.canRead() && rootFile.getName().endsWith(".jar")) {
            jarFileList.add(rootFile);
        }
    }

    /**
     * 删除指定目录中的某个文件
     *
     * @param baseDir  要删除文件的目录
     * @param fileName 要删除文件的文件名称
     * @return true 删除成功
     */
    public static void deleteFileInDir(String baseDir, String fileName) throws IOException {
        if (StringUtils.isBlank(fileName) || StringUtils.isBlank(baseDir)) {
            return;
        }
        Files.deleteIfExists(new File(baseDir + File.separator + fileName).toPath());
    }

    /**
     * 把指定文件复制到指定目录，覆盖方式
     *
     * @param srcFile 要复制的源文件
     * @param distDir 复制到的目录，注意这个要复制到的目录一定要存在，不然会报错
     * @throws IOException
     */
    public static void transferFileTo(File srcFile, String distDir) throws IOException {
        if (!srcFile.exists()) {
            log.error("要复制到目录 [{}] 的源文件 [{}] 不存在", distDir, srcFile.getAbsolutePath());
            throw new IOException("复制到目录 [" + distDir + "] 的源文件 [" + srcFile.getAbsolutePath() + "] 不存在");
        }
        try (final FileChannel open = FileChannel.open(srcFile.toPath(), StandardOpenOption.READ);
             final FileChannel distChannel = FileChannel.open(Paths.get(distDir + File.separator + srcFile.getName()),
                 StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            open.transferTo(0, srcFile.length(), distChannel);
        }
    }

    /**
     * 读取文件的内容
     *
     * @param file 要读取内容的指定文件
     * @return null -> 文件内容为空或文件不存在
     */
    public static String readFileContent(File file) {
        if (!file.exists()) {
            return null;
        }

        try (final BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
             final ByteArrayOutputStream os = new ByteArrayOutputStream(2048)) {
            int len;
            byte[] buffer = new byte[2048];
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取文件 [{}] 内容异常", file.getAbsolutePath(), e);
        }

        return null;
    }

}
