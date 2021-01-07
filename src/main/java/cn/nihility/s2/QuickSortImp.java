package cn.nihility.s2;

public class QuickSortImp<T extends Comparable<T>> implements ISort<T> {
    @Override
    public void sort(T[] array) {
        quickSort3(array, 0, array.length - 1);
    }

    public static void main(String[] args) {
        SortUtil.sort10(new QuickSortImp<>());
    }

    private void quickSort3(T[] array, int lo, int hi) {
        if (hi <= lo) {
            return;
        }
        int middle = innerSort3(array, lo, hi);
        quickSort3(array, lo, middle - 1);
        quickSort3(array, middle + 1, hi);
    }

    private int innerSort3(T[] array, int lo, int hi) {
        int l = lo;
        int h = hi + 1;
        T pivot = array[lo];

        while (true) {
            while (SortUtil.lt(array[++l], pivot)) {
                if (l == hi) break;
            }

            while (SortUtil.lt(pivot, array[--h])) {
                if (h == lo) break;
            }

            if (l >= h) break;

            SortUtil.swap(array, l, h);
        }

        SortUtil.swap(array, lo, h);
        // array[lo .. h-1] <= array[h] <= array[h+1 .. hi]
        return h;
    }

    private void quickSort2(T[] array, int left, int right) {
        if (left < right) {
            int middle = innerSort2(array, left, right);
            quickSort2(array, left, middle - 1);
            quickSort2(array, middle, right);
        }
    }

    private int innerSort2(T[] array, int left, int right) {
        int pivot = (left + right) / 2;
        int lIndex = left, rIndex = right;
        T pivotItem = array[pivot];

        while (lIndex <= rIndex) {
            while (lIndex <= rIndex && SortUtil.lt(array[lIndex], pivotItem)) {
                lIndex++;
            }
            while (lIndex <= rIndex && SortUtil.gt(array[rIndex], pivotItem)) {
                rIndex--;
            }
            if (lIndex <= rIndex) {
                SortUtil.swap(array, lIndex++, rIndex--);
            }
        }

        return lIndex;
    }

    private void quickSort(T[] array, int left, int right) {
        if (left < right) {
            int middle = innerSort(array, left, right);
            quickSort(array, left, middle);
            quickSort(array, middle + 1, right);
        }
    }

    private int innerSort(T[] array, int left, int right) {
        int lIndex = left, rIndex = right;
        T pivotItem = array[left];

        while (lIndex < rIndex) {
            while (lIndex < rIndex && SortUtil.le(pivotItem, array[rIndex])) {
                rIndex--;
            }
            array[lIndex] = array[rIndex];
            while (lIndex < rIndex && SortUtil.ge(pivotItem, array[lIndex])) {
                lIndex++;
            }
            array[rIndex] = array[lIndex];
        }

        array[lIndex] = pivotItem;

        return lIndex;
    }
}
