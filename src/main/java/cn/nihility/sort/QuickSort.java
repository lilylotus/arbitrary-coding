package cn.nihility.sort;

import cn.nihility.util.CommonUtil;

public class QuickSort<T extends Comparable<T>> implements ISort<T> {
    @Override
    public void sort(T[] array) {
//        quickSort(array, 0, array.length - 1);
        quickSort2(array, 0, array.length - 1);
    }

    private void quickSort(T[] array, int left, int right) {
        if (left < right) {
            int mid = innerSort(array, left, right);
            quickSort(array, left, mid);
            quickSort(array, mid + 1, right);
        }
    }

    private int innerSort(T[] array, int left, int right) {
        int lIndex = left, rIndex = right;
        T pivot = array[left];

        while (lIndex < rIndex) {
            while (lIndex < rIndex && CommonUtil.le(pivot, array[rIndex])) {
                rIndex--;
            }
            array[lIndex] = array[rIndex];
            while (lIndex < rIndex && CommonUtil.le(array[lIndex], pivot)) {
                lIndex++;
            }
            array[rIndex] = array[lIndex];
        }

        array[lIndex] = pivot;

        return  lIndex;
    }

    private void quickSort2(T[] array, int left, int right) {
        if (left < right) {
            int mid = innerSort2(array, left, right);
            quickSort2(array, left, mid - 1);
            quickSort2(array, mid, right);
        }
    }

    private int innerSort2(T[] array, int left, int right) {
        int pivot = (left + right) / 2;
        T tmp = array[pivot];
        while (left <= right) {
            while (left <= right && CommonUtil.lt(array[left], tmp)) {
                left++;
            }

            while (left <= right && CommonUtil.lt(tmp, array[right])) {
                right--;
            }

            if (left <= right) {
                CommonUtil.swap(array, left++, right--);
            }
        }
        return left;
    }

    public static void main(String[] args) {
        CommonUtil.sortArray(new QuickSort<>());
    }
}
