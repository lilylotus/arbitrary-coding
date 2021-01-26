package cn.nihility.pattern;

/**
 * 适配器模式
 */
public class Adapter {

    public static void main(String[] args) {
        Target target = new ClassAdapter();
        target.request();
    }

    /**
     * 目标接口
     */
    interface Target {
        void request();
    }

    /**
     * 适配者接口
     */
    static class Adaptee {
        public void specificRequest() {
            System.out.println("适配者中的业务代码被调用！");
        }
    }

    /**
     * 类适配器类
     */
    static class ClassAdapter extends Adaptee implements Target {
        @Override
        public void request() {
            // 增强原始 Adaptee 类功能
            System.out.println("增强原始 Adaptee 类功能");
            specificRequest();
        }
    }

}
