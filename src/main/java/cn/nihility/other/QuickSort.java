package cn.nihility.other;

public class QuickSort {

    public static void sort(int[] array) {
//        quickSort(array, 0, array.length - 1);
        quickSort2(array, 0, array.length - 1);
    }

    public static void quickSort(int[] array, int left, int right) {
        if (left < right) {
            int mid = innerSort(array, left, right);
            quickSort(array, left, mid);
            quickSort(array, mid + 1, right);
        }
    }

    public static int innerSort(int[] array, int left, int right) {
        int lIndex = left, rIndex = right;
        int povit = array[left];

        while (lIndex < rIndex) {
            while (lIndex < rIndex && array[rIndex] >= povit) {
                rIndex--;
            }
            array[lIndex] = array[rIndex];
            while (lIndex < rIndex && array[lIndex] <= povit) {
                lIndex++;
            }
            array[rIndex] = array[lIndex];

//            OtherUtil.swap(array, lIndex, rIndex);
        }

        array[lIndex] = povit;
        return lIndex;
    }

    public static void quickSort2(int[] array, int left, int right) {
        if (left < right) {
            int mid = innerSort2(array, left, right);
            quickSort(array, left, mid - 1);
            quickSort(array, mid, right);
        }
    }

    public static int innerSort2(int[] array, int left, int right) {
        int lIndex = left, rIndex = right;
        int mid = (left + right) / 2;
        int pivot = array[mid];

        while (lIndex <= rIndex) {
            while (lIndex <= rIndex && array[lIndex] < pivot) {
                lIndex++;
            }
            while (lIndex <= rIndex && array[rIndex] > pivot) {
                rIndex--;
            }

            if (lIndex <= rIndex) {
                OtherUtil.swap(array, rIndex--, lIndex++);
            }
        }

        return lIndex;
    }

    public static void main(String[] args) {
        int[] randIntArray = OtherUtil.randIntArray(20, 10, 100);
        OtherUtil.print(randIntArray);
        sort(randIntArray);
        OtherUtil.print(randIntArray);

    }

}
