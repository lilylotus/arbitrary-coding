package cn.nihility.sort;

import cn.nihility.util.CommonUtil;

public class SelectSort<T extends Comparable<T>> implements ISort<T> {
    @Override
    public void sort(T[] array) {
        int len = array.length;
        int minIndex;

        for (int loop = 0, cnt = len - 1; loop < cnt; loop++) {
            minIndex = loop;

            for (int j = loop + 1; j < len; j++) {
                if (CommonUtil.gt(array[minIndex], array[j])) {
                    minIndex = j;
                }
            }

            if (minIndex != loop) {
                CommonUtil.swap(array, minIndex, loop);
            }
        }

    }

    public static void main(String[] args) {
        CommonUtil.sortArray(new SelectSort<>());
    }
}
