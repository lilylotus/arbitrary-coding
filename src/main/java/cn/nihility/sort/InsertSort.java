package cn.nihility.sort;

import cn.nihility.util.CommonUtil;

public class InsertSort<T extends Comparable<T>> implements ISort<T> {
    @Override
    public void sort(T[] array) {
        int len = array.length;
        T tmp;
        int index;

        for (int loop = 1; loop < len; loop++) {
            tmp = array[loop];
            index = loop - 1;
            while (index >= 0 && CommonUtil.gt(array[index], tmp)) {
                array[index + 1] = array[index];
                index--;
            }
            array[index + 1] = tmp;
        }

    }

    public static void main(String[] args) {
        CommonUtil.sortArray(new InsertSort<>());
    }
}
