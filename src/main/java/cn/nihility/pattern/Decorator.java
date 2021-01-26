package cn.nihility.pattern;

/**
 * 装饰器模式
 */
public class Decorator {

    public static void main(String[] args) {
        Component p = new ConcreteComponent();
        p.operation();

        System.out.println("---------------------------------");

        Component d = new ConcreteDecorator(p);
        d.operation();
    }

    /**
     * 抽象构件角色
     */
    interface Component {
        void operation();
    }

    /**
     * 具体构件角色
     */
    static class ConcreteComponent implements Component {
        public ConcreteComponent() { System.out.println("创建具体构件角色"); }

        @Override
        public void operation() { System.out.println("调用具体构件角色的方法operation()"); }
    }

    /**
     * 抽象装饰角色
     */
    static class DecoratorRole implements Component {

        private Component component;

        public DecoratorRole(Component component) {
            this.component = component;
        }

        public void operation() {
            component.operation();
        }
    }

    /**
     * 具体装饰角色
     */
    static class ConcreteDecorator extends DecoratorRole {
        public ConcreteDecorator(Component component) {
            super(component);
        }

        @Override
        public void operation() {
            super.operation();
            addedFunction();
        }

        public void addedFunction() {
            System.out.println("为具体构件角色增加额外的功能addedFunction()");
        }
    }

}
