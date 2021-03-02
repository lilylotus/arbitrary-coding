package cn.nihility.s2;

public class MergeSortImpl<T extends Comparable<T>> implements ISort<T> {
    @Override
    public void sort(T[] array) {
        T[] tmp = (T[]) new Comparable[array.length];
        mergeSort(array, tmp, 0, array.length - 1);
    }

    public static void main(String[] args) {
        SortUtil.sort(new MergeSortImpl<>());
    }

    private void mergeSort(T[] array, T[] tmp, int left, int right) {
        if (right <= left) {
            return;
        }

        int middle = (left + right) / 2;
        //int middle = left + (right - left) / 2;
        mergeSort(array, tmp, left, middle);
        mergeSort(array, tmp, middle + 1, right);
        //mergeInner(array, tmp, left, middle, right);
        //mergeInner2(array, left, middle, right);
        merge(array, tmp, left, middle, right);

    }

    private void merge(T[] array, T[] tmp, int left, int mid, int right) {

        System.arraycopy(array, left, tmp, left, right + 1 - left);

        int lIndex = left, mIndex = mid + 1;
        for (int i = left; i <= right; i++) {
            if (lIndex > mid)                                  array[i] = tmp[mIndex++];
            else if (mIndex > right)                           array[i] = tmp[lIndex++];
            else if (SortUtil.lt(tmp[mIndex], tmp[lIndex]))    array[i] = tmp[mIndex++];
            else                                               array[i] = tmp[lIndex++];
        }
    }

    private void mergeInner2(T[] array, int left, int mid, int right) {
        int lIndex = left, mIndex = mid + 1, index = 0;
        int len = right - left + 1;
        T[] tmp = (T[]) new Comparable[len];

        while (lIndex <= mid && mIndex <= right) {
            if (SortUtil.lt(array[lIndex], array[mIndex])) {
                tmp[index++] = array[lIndex++];
            } else {
                tmp[index++] = array[mIndex++];
            }
        }
        while (lIndex <= mid) {
            tmp[index++] = array[lIndex++];
        }
        while (mIndex <= right) {
            tmp[index++] = array[mIndex++];
        }

        System.arraycopy(tmp, 0, array, left, len);
    }

    private void mergeInner(T[] array, T[] tmp, int left, int mid, int right) {
        int lIndex = left, mIndex = mid + 1, index = left;
        //System.arraycopy(array, left, tmp, left, right - left + 1);

        while (lIndex <= mid && mIndex <= right) {
            if (SortUtil.lt(array[lIndex], array[mIndex])) {
                tmp[index++] = array[lIndex++];
            } else {
                tmp[index++] = array[mIndex++];
            }
        }
        while (lIndex <= mid) {
            tmp[index++] = array[lIndex++];
        }
        while (mIndex <= right) {
            tmp[index++] = array[mIndex++];
        }

        System.arraycopy(tmp, left, array, left, right + 1 - left);
        /*for (int loop = left; loop <= right; loop++) {
            array[loop] = tmp[loop];
        }*/
    }
}
