package cn.nihility.sort;

import cn.nihility.util.CommonUtil;
import cn.nihility.util.ISort;

public class BubbleSort<T extends Comparable<T>> implements ISort<T> {


    @Override
    public void sort(T[] array) {
        int len = array.length;
        boolean change = false;

        for (int loop = 0, op = len - 1; loop < op; loop++) {
            for (int i = 0, ii = len - loop - 1; i < ii; i++) {
                if (CommonUtil.gt(array[i], array[i + 1])) {
                    CommonUtil.swap(array, i, i + 1);
                    change = true;
                }
            }
            if (!change) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        CommonUtil.sortArray(new BubbleSort<>());
    }

}
