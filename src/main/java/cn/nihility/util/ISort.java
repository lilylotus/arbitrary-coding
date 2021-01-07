package cn.nihility.util;

public interface ISort<T extends Comparable<T>> {
    void sort(T[] array);
}
