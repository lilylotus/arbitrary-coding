package cn.nihility.s2;

public class BubbleSortImpl<T extends Comparable<T>> implements ISort<T> {

    @Override
    public void sort(T[] array) {
        int len = array.length;
        boolean swap;

        for (int i = 0; i < len; i++) {
            swap = false;
            for (int j = 0; j < len - 1; j++) {
                if (SortUtil.gt(array[j], array[j + 1])) {
                    SortUtil.swap(array, j, j + 1);
                    swap = true;
                }
            }
            if (!swap) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        /*ISort<SortItem> sort = new BubbleSortImpl<>();
        SortUtil.sort(sort, SortUtil.generateIntArray(20, 0, 100));*/

        SortUtil.sort(new BubbleSortImpl<>());
    }

}
