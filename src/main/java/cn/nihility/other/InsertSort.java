package cn.nihility.other;

public class InsertSort {

    public static void sort(int[] array) {

        int len = array.length;
        int index;
        int tmp;

        for (int i = 1; i < len; i++) {
            tmp = array[i];
            index = i - 1;

            while (index >= 0 && array[index] > tmp) {
                array[index + 1] = array[index];
                index--;
            }

            array[index + 1] = tmp;

        }


    }

    public static void main(String[] args) {
        int[] randIntArray = OtherUtil.randIntArray(20, 10, 100);
        OtherUtil.print(randIntArray);
        sort(randIntArray);
        OtherUtil.print(randIntArray);

    }

}
