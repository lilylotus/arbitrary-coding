package cn.nihility.other;

public class SelectSort {

    public static void sort(int[] array) {
        int len = array.length;
        int min;
        for (int loop = 0, last = array.length - 1; loop < last; loop++) {
            min = loop;
            for (int i = loop + 1; i < len; i++) {
                if (array[i] < array[min]) {
                    min = i;
                }
            }

            if (min != loop) {
                OtherUtil.swap(array, min, loop);
            }

        }
    }

    public static void main(String[] args) {
        int[] randIntArray = OtherUtil.randIntArray(20, 10, 100);
        OtherUtil.print(randIntArray);
        sort(randIntArray);
        OtherUtil.print(randIntArray);

    }

}
