package cn.nihility.other;

public class MergeSort {

    public static void sort(int[] array) {
        /*int[] tmp = new int[array.length];
        merge(array, tmp, 0, array.length - 1);*/
        merge2(array, 0, array.length - 1);
    }


    public static void merge(int[] array, int[] tmp, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            merge(array, tmp, left, mid);
            merge(array, tmp, mid + 1, right);
            mergeSort(array, tmp, left, mid, right);
        }

    }

    public static void mergeSort(int[] array, int[] tmp, int left, int mid, int right) {
        int lIndex = left, mIndex = mid + 1;
        int index = left;
        System.arraycopy(array, left, tmp, left, right - left + 1);

        while (lIndex <= mid && mIndex <= right) {
            if (tmp[lIndex] > tmp[mIndex]) {
                array[index++] = tmp[mIndex++];
            } else {
                array[index++] = tmp[lIndex++];
            }
        }

        while (lIndex <= mid) {
            array[index++] = tmp[lIndex++];
        }
        while (mIndex <= right) {
            array[index++] = tmp[mIndex++];
        }

    }

    public static void merge2(int[] array, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            merge2(array, left, mid);
            merge2(array, mid + 1, right);
            mergeSort2(array, left, mid, right);
        }
    }

    public static void mergeSort2(int[] array, int left, int mid, int right) {
        int lIndex = left, mIndex = mid + 1;
        int index = 0;
        int len = right - left + 1;
        int[] tmp = new int[len];
        System.arraycopy(array, left, tmp, 0, len);

        while (lIndex <= mid && mIndex <= right) {
            if (array[lIndex] > array[mIndex]) {
                tmp[index++] = array[mIndex++];
            } else {
                tmp[index++] = array[lIndex++];
            }
        }

        while (lIndex <= mid) {
            tmp[index++] = array[lIndex++];
        }
        while (mIndex <= right) {
            tmp[index++] = array[mIndex++];
        }

        for (int i = 0; i < index; i++) {
            array[left + i] = tmp[i];
        }

    }

    public static void main(String[] args) {
        int[] randIntArray = OtherUtil.randIntArray(20, 10, 100);
        OtherUtil.print(randIntArray);
        sort(randIntArray);
        OtherUtil.print(randIntArray);

    }

}
