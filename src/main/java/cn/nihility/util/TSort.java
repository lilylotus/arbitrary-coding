package cn.nihility.util;

public interface TSort<T extends Comparable<T>> {
    void sort(T[] array);
}
