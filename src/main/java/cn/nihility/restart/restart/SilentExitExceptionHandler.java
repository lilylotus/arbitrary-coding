package cn.nihility.restart.restart;

import java.util.Arrays;

public class SilentExitExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler delegate;

    SilentExitExceptionHandler(Thread.UncaughtExceptionHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        if (exception instanceof SilentExitExceptionHandler.SilentExitException) {
            if (isJvmExiting(thread)) {
                preventNonZeroExitCode();
            }
            return;
        }
        if (this.delegate != null) {
            this.delegate.uncaughtException(thread, exception);
        }
    }

    private boolean isJvmExiting(Thread exceptionThread) {
        for (Thread thread : getAllThreads()) {
            if (thread != exceptionThread && thread.isAlive() && !thread.isDaemon()) {
                return false;
            }
        }
        return true;
    }

    protected Thread[] getAllThreads() {
        ThreadGroup rootThreadGroup = getRootThreadGroup();
        Thread[] threads = new Thread[32];
        int count = rootThreadGroup.enumerate(threads);
        while (count == threads.length) {
            threads = new Thread[threads.length * 2];
            count = rootThreadGroup.enumerate(threads);
        }
        return Arrays.copyOf(threads, count);
    }

    private ThreadGroup getRootThreadGroup() {
        ThreadGroup candidate = Thread.currentThread().getThreadGroup();
        while (candidate.getParent() != null) {
            candidate = candidate.getParent();
        }
        return candidate;
    }

    protected void preventNonZeroExitCode() {
        System.exit(0);
    }

    public static void setup(Thread thread) {
        Thread.UncaughtExceptionHandler handler = thread.getUncaughtExceptionHandler();
        if (!(handler instanceof SilentExitExceptionHandler)) {
            handler = new SilentExitExceptionHandler(handler);
            thread.setUncaughtExceptionHandler(handler);
        }
    }

    public static void exitCurrentThread() {
        throw new SilentExitExceptionHandler.SilentExitException();
    }

    private static class SilentExitException extends RuntimeException {
        private static final long serialVersionUID = 4286239488334571703L;
    }

}
