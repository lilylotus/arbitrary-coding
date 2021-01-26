package cn.nihility.pattern.factory;

/**
 * 抽象工厂模式
 */
public class AbstractFactory {

    public static void main(String[] args) {
        Farm farm = new ShanghaiFarm();
        farm.showFarm();

        System.out.println("-------------");
        farm = new BeijingFarm();
        farm.showFarm();
    }

    /**
     * 抽象产品：动物类
     */
    interface Animal {
        void show();
    }

    /**
     * 抽象产品：植物类
     */
    interface Plant {
        void show();
    }

    /**
     * 抽象工厂：农场类
     */
    interface Farm {
        Animal createAnimal();
        Plant createPlant();
        default void showFarm() {
            createAnimal().show();
            createPlant().show();
        }
    }

    /**
     * 具体动物 马
     */
    static class Horse implements Animal {
        @Override
        public void show() { System.out.println("具体动物 马"); }
    }

    /**
     * 具体动物 牛
     */
    static class Cattle implements Animal {
        @Override
        public void show() { System.out.println("具体动物 牛"); }
    }

    /**
     * 具体植物 水果类
     */
    static class Fruitage implements Plant {
        @Override
        public void show() { System.out.println("具体植物 水果类"); }
    }

    /**
     * 具体植物 蔬菜类
     */
    static class Vegetables implements Plant {
        @Override
        public void show() { System.out.println("具体植物 蔬菜类"); }
    }

    /**
     * 具体的农场，上海农场
     */
    static class ShanghaiFarm implements Farm {
        @Override
        public Animal createAnimal() {
            System.out.println("上海农场生产动物 马");
            return new Horse();
        }

        @Override
        public Plant createPlant() {
            System.out.println("上海农场生产植物 水果类");
            return new Fruitage();
        }
    }

    /**
     * 具体的农场，北京农场
     */
    static class BeijingFarm implements Farm {
        @Override
        public Animal createAnimal() {
            System.out.println("北京农场生产动物 牛");
            return new Cattle();
        }

        @Override
        public Plant createPlant() {
            System.out.println("北京农场生产植物 蔬菜类");
            return new Vegetables();
        }
    }

}
