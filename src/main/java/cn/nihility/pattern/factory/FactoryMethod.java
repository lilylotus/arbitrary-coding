package cn.nihility.pattern.factory;

/**
 * 工厂方法模式
 */
public class FactoryMethod {

    public static void main(String[] args) {
        AbstractFactory factory = new ConcreteFactory1();
        factory.createProduct().show();

        System.out.println("------------------");
        factory = new ConcreteFactory2();
        factory.createProduct().show();
    }

    /**
     * 抽象产品，提供产品生成接口
     */
    interface Product {
        void show();
    }

    /**
     * 具体的产品，实现抽象产品中的抽象方法
     */
    static class ConcreteProduct1 implements Product {
        @Override
        public void show() {
            System.out.println("具体产品 1 展示");
        }
    }

    static class ConcreteProduct2 implements Product {
        @Override
        public void show() {
            System.out.println("具体产品 2 展示");
        }
    }

    /**
     * 抽象工厂：提供了厂品的生成方法
     */
    interface AbstractFactory {
        Product createProduct();
    }

    /**
     * 具体工厂 1：实现了厂品的生成方法
     */
    static class ConcreteFactory1 implements AbstractFactory {
        @Override
        public Product createProduct() {
            System.out.println("具体工厂 1 生产具体产品 1");
            return new ConcreteProduct1();
        }
    }

    static class ConcreteFactory2 implements AbstractFactory {
        @Override
        public Product createProduct() {
            System.out.println("具体工厂 2 生产具体产品 2");
            return new ConcreteProduct2();
        }
    }

}
