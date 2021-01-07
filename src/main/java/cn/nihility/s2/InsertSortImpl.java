package cn.nihility.s2;

public class InsertSortImpl<T extends Comparable<T>> implements ISort<T> {
    @Override
    public void sort(T[] array) {
        //insertSort2(array);
        insertSort(array, 0, array.length);
    }

    private void insertSort(T[] array) {
        int len = array.length;
        T tmp;
        int index;

        for (int loop = 1; loop < len; loop++) {
            tmp = array[loop];
            index = loop - 1;
            while (index >= 0 && SortUtil.lt(tmp, array[index])) {
                array[index + 1] = array[index];
                index--;
            }
            array[index + 1] = tmp;
        }
    }

    private void insertSort2(T[] array) {
        int len = array.length;
        for (int loop = 1; loop < len; loop++) {
            for (int j = loop; j > 0 && SortUtil.lt(array[j], array[j - 1]); j--) {
                SortUtil.swap(array, j, j - 1);
            }
        }
    }

    /**
     * 插入排序
     * @param array 要排序的数组
     * @param lo 开始排序数组的下标，包含
     * @param hi 结束排序数组的下标，不包含
     */
    private void insertSort(T[] array, int lo, int hi) {
        for (int loop = lo + 1; loop < hi; loop++) {
            for (int j = loop; j > 0 && SortUtil.lt(array[j], array[j - 1]); j--) {
                SortUtil.swap(array, j, j - 1);
            }
        }
    }

    public static void main(String[] args) {
        SortUtil.sort(new InsertSortImpl<>());
    }
}
