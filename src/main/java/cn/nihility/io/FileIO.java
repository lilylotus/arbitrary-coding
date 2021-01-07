package cn.nihility.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileIO {

    public static void main(String[] args) throws IOException {


        final Path dir = Paths.get("D:/nio");

        if (Files.exists(dir)) {
            System.out.println(dir.toString() + " 存在");
        } else {
            System.out.println(dir.toString() + " 不存在");
            Files.createDirectories(dir);
        }

        final Path path = Paths.get("D:/nio/", "nio.txt");
        if (Files.exists(path)) {
            System.out.println(path.toString() + " 存在");
        } else {
            System.out.println(path.toString() + " 不存在");
            Files.createFile(path);
        }

        List<String> lines = new ArrayList<>();
        lines.add("第一行");
        lines.add(null);
        lines.add("第三行");
        lines.add("");

        Files.write(path, lines, StandardOpenOption.APPEND);
        Files.write(path, "鸟后".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);


        final Path path2 = Paths.get("D:/nio/", "nio2.txt");

        Files.copy(path, path2, StandardCopyOption.REPLACE_EXISTING);

        // BST
        // AVL
    }

}
