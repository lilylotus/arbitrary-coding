package cn.nihility.test;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FileTest {

    public static void main(String[] args) {
        String f = "D:\\kingbase8-8.2.0.jar";
        File file = new File(f);

        System.out.println(file.length());
        System.out.println(file.lastModified());

        System.out.println(LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()));
    }

}
