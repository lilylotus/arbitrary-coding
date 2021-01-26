package cn.nihility.pattern.factory;

/**
 * 简单工厂方法模式，不属于 GOF 设计模式种的一种
 */
public class SimpleFactoryMethod {

    private SimpleFactoryMethod() {}

    public static Product getProduct(String type) {
        if ("a".equalsIgnoreCase(type)) {
            return new ConcreteProductA();
        } else if ("b".equalsIgnoreCase(type)) {
            return new ConcreteProductB();
        }

        return null;
    }

    public static void main(String[] args) {
        getProduct("b").show();
        getProduct("a").show();
    }

    /**
     * 抽象的产品
     */
    public interface Product {
        void show();
    }

    /**
     * 具体的产品
     */
    static class ConcreteProductA implements Product {
        @Override
        public void show() {
            System.out.println("ConcreteProductA");
        }
    }

    static class ConcreteProductB implements Product {
        @Override
        public void show() {
            System.out.println("ConcreteProductB");
        }
    }

}
