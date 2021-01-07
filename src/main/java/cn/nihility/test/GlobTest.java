package cn.nihility.test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class GlobTest {

    public static void main(String[] args) throws IOException {
        String glob = "glob:**/rolling-file-trace*.gz";
        String path = "D:/logger/urm/biz/20200827";
        match(glob, path);
    }

    public static void match(String glob, String location) throws IOException {

        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);

        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                System.out.println("visitFile path [" + path + "]");
                if (pathMatcher.matches(path)) {
                    System.out.println("================ visitFile matches path [" + path + "]");
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }


}
