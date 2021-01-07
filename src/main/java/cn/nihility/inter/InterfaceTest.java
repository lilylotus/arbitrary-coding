package cn.nihility.inter;

public class InterfaceTest {
    public static void main(String[] args) {
        Colleague colleague = new Colleague();
        colleague.eat();
        colleague.talk();

        People.peopleStatic();
    }

    interface People {
        default void eat() { System.out.println("People eat"); }
        static void peopleStatic() { System.out.println("People Static"); }
    }

    interface Man extends People {
        @Override default void eat() { System.out.println("Man eat"); }
        static void manStatic() { System.out.println("Man Static"); }
        /* 因为 Boy 的重新，该抽象方法会提示未被使用 */
        void talk();
    }

    interface Boy extends People, Man {
        /* 这时候会覆盖掉 Man 的 talk 方法还变为了默认方法 */
        @Override default void talk() { System.out.println("Boy talk"); }
    }

    static class Colleague implements Boy { }

}
