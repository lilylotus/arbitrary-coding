package cn.nihility.test;


import java.util.Optional;

public class OptionalsTest {

    public static void main(String[] args) {

        OptionalBo optional = new OptionalBo();
        optional.setItem("sss");

        String aNull = Optional.ofNullable(optional)
                .map(OptionalBo::getItem).orElse("null1");

        System.out.println(aNull);
    }

    static class OptionalBo {
        String item;
        String item1;
        String item2;
        String item3;
        String item4;

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public String getItem1() {
            return item1;
        }

        public void setItem1(String item1) {
            this.item1 = item1;
        }

        public String getItem2() {
            return item2;
        }

        public void setItem2(String item2) {
            this.item2 = item2;
        }

        public String getItem3() {
            return item3;
        }

        public void setItem3(String item3) {
            this.item3 = item3;
        }

        public String getItem4() {
            return item4;
        }

        public void setItem4(String item4) {
            this.item4 = item4;
        }
    }
}
