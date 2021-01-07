package cn.nihility.test;

public class ThreadLocalTest {

    public static void main(String[] args) {
        ThreadLocal<String> t = new ThreadLocal<>();

        t.set("Thread Local");

    }

}
