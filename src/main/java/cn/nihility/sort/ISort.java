package cn.nihility.sort;

public interface ISort<T extends Comparable<T>> {
    void sort(T[] array);
}
