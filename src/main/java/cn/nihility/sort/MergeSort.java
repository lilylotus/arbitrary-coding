package cn.nihility.sort;

import cn.nihility.util.CommonUtil;
import cn.nihility.util.ISort;

import java.util.Comparator;

public class MergeSort<T extends Comparable<T>> implements ISort<T> {
    @Override
    public void sort(T[] array) {
        T[] tmp = (T[]) new Comparable[array.length];
        merge(array, tmp, 0, array.length - 1);
    }

    public static void main(String[] args) {
        CommonUtil.sortArray(new MergeSort<>());
    }

    private void merge(T[] array, T[] tmp, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            merge(array, tmp, left, mid);
            merge(array, tmp, mid + 1, right);
//            innerMerge(array, tmp, left, mid, right);
            innerMerge2(array, left, mid, right);
        }
    }

    private void innerMerge(T[] array, T[] tmp, int left, int mid, int right) {
        int lIndex = left, rIndex = mid + 1, index = left;
        System.arraycopy(array, left, tmp, left, (right - left + 1));

        while (lIndex <= mid && rIndex <= right) {
            if (CommonUtil.lt(array[lIndex], array[rIndex])) {
                tmp[index++] = array[lIndex++];
            } else {
                tmp[index++] = array[rIndex++];
            }
        }

        while (lIndex <= mid) {
            tmp[index++] = array[lIndex++];
        }

        while (rIndex <= right) {
            tmp[index++] = array[rIndex++];
        }

        for (int i = left; i <= right; i++) {
            array[i] = tmp[i];
        }
    }

    private void innerMerge2(T[] array, int left, int mid, int right) {
        int lIndex = left, rIndex = mid + 1, index = 0;
        T[] tmp = (T[]) new Comparable[right - left + 1];
        System.arraycopy(array, left, tmp, 0, (right - left + 1));

        while (lIndex <= mid && rIndex <= right) {
            if (CommonUtil.lt(array[lIndex], array[rIndex])) {
                tmp[index++] = array[lIndex++];
            } else {
                tmp[index++] = array[rIndex++];
            }
        }

        while (lIndex <= mid) {
            tmp[index++] = array[lIndex++];
        }

        while (rIndex <= right) {
            tmp[index++] = array[rIndex++];
        }

        for (int loop = 0; loop < index; loop++) {
            array[loop + left] = tmp[loop];
        }
    }
}
