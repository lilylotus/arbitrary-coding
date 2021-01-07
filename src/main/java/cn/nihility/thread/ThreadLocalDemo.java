package cn.nihility.thread;

public class ThreadLocalDemo {

    public static void main(String[] args) {
        ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "Initialization");

        System.out.println(threadLocal.get());

        threadLocal.set("newValue");

        System.out.println(threadLocal.get());

        threadLocal.remove();
    }

}
