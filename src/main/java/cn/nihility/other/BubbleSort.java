package cn.nihility.other;

public class BubbleSort {

    public static void sort(int[] array) {
        int len = array.length;
        boolean swap;

        for (int i = 0, ii = array.length - 1; i < ii; i++) {
            swap = false;

            for (int j = 0, jj = len - i - 1; j < jj; j++) {
                if (array[j] > array[j + 1]) {
                    OtherUtil.swap(array, j, j + 1);
                    swap = true;
                }
            }

            if (!swap) {
                break;
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
