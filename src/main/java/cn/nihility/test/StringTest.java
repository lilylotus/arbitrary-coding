package cn.nihility.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

public class StringTest {

    public static void main(String[] args) {
        Inner inner = new Inner("");

        System.out.println(Objects.equals(null, inner.getValue()));
        System.out.println(Objects.equals(null, InnerEnum.OTHER.getValue()));
    }

    static class Inner {
        String value;

        public Inner(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    enum InnerEnum {
        OTHER("");

        private String value;

        InnerEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static void main2(String[] args) {
        String split = "a,b,c,d,,";
        final String[] sp = split.split(",", -1);

        System.out.println(sp.length);
        for (String s : sp) {
            System.out.println("[" + s + "]");
        }

        List<String> list = new ArrayList<>();
        list.add("one");
        list.add("tow");

        String[] sa = new String[2];
        final String[] strings = list.toArray(sa);


        for (String s : sa) {
            System.out.println(s);
        }
        System.out.println("=========");
        for (String string : strings) {
            System.out.println(string);
        }

        for (String s : list) {
            if ("one".equals(s)) {
                list.remove(s);
            }
        }

        LongAdder la = new LongAdder();
//        la.add(100L);
        System.out.println(la.toString());
        la.add(Long.MAX_VALUE);
        System.out.println(la.toString());
    }

}
