package cn.nihility.s2;

public class SelectSortImpl<T extends Comparable<T>> implements ISort<T> {
    @Override
    public void sort(T[] array) {
        int len = array.length;
        int minIndex;

        for (int loop = 0; loop < len; loop++) {
            minIndex = loop;
            for (int index = loop + 1; index < len; index++) {
                if (SortUtil.lt(array[index], array[minIndex])) {
                    minIndex = index;
                }
            }
            if (minIndex != loop) {
                SortUtil.swap(array, minIndex, loop);
            }
        }
    }

    public static void main(String[] args) {
        SortUtil.sort(new SelectSortImpl<>());
    }
}
