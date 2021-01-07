package cn.nihility.s2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public class SortUtil {

    public static <T> void swap(T[] array, int xIndex, int yIndex) {
        if (xIndex < 0 || yIndex < 0) {
            throw new IllegalArgumentException("Array index cannot be lower then zero");
        }
        T tmp = array[xIndex];
        array[xIndex] = array[yIndex];
        array[yIndex] = tmp;
    }

    public static <T extends Comparable<T>> boolean eq(T x, T y) {
        return x != null && (x.compareTo(y) == 0);
    }

    public static <T extends Comparable<T>> boolean eq(T x, T y, Comparator<T> comparator) {
        return comparator.compare(x, y) == 0;
    }

    public static <T extends Comparable<T>> boolean gt(T x, T y) {
        return x != null && (x.compareTo(y) > 0);
    }

    public static <T extends Comparable<T>> boolean gt(T x, T y, Comparator<T> comparator) {
        return comparator.compare(x, y) > 0;
    }

    public static <T extends Comparable<T>> boolean lt(T x, T y) {
        return x != null && (x.compareTo(y) < 0);
    }

    public static <T extends Comparable<T>> boolean lt(T x, T y, Comparator<T> comparator) {
        return comparator.compare(x, y) < 0;
    }

    public static <T extends Comparable<T>> boolean ge(T x, T y) {
        return x != null && (x.compareTo(y) >= 0);
    }

    public static <T extends Comparable<T>> boolean ge(T x, T y, Comparator<T> comparator) {
        return comparator.compare(x, y) >= 0;
    }

    public static <T extends Comparable<T>> boolean le(T x, T y) {
        return x != null && (x.compareTo(y) <= 0);
    }

    public static <T extends Comparable<T>> boolean le(T x, T y, Comparator<T> comparator) {
        return comparator.compare(x, y) <= 0;
    }

    public static <T> void printArray(T[] array) {
        if (null == array) {
            throw new IllegalArgumentException("param array cannot be null");
        }
        System.out.println(Arrays.stream(array).map(Object::toString).collect(Collectors.joining(", ")));
    }

    public static SortItem[] generateIntArray(int len, int min, int max) {
        SortItem[] array = new SortItem[len];
        Random random = new Random(System.currentTimeMillis());
        int gap = max - min + 1;
        for (int i = 0; i < len; i++) {
            array[i] = new SortItem(i + 1, random.nextInt(gap) + min);
        }
        return array;
    }

    public static <T extends Comparable<T>> void sort(ISort<T> sort, T[] array) {
        printArray(array);
        sort.sort(array);
        printArray(array);
    }

    public static void sort(ISort<SortItem> sort) {
        SortItem[] array = generateIntArray(20, 0, 100);
        printArray(array);
        sort.sort(array);
        printArray(array);
    }

    public static void sort10(ISort<SortItem> sort) {
        SortItem[] array = generateIntArray(10, 0, 100);
        printArray(array);
        sort.sort(array);
        printArray(array);
    }

    public static void sort100(ISort<SortItem> sort) {
        SortItem[] array = generateIntArray(100, 0, 100);
        printArray(array);
        sort.sort(array);
        printArray(array);
    }

    public static void main(String[] args) {
        System.out.println(ge(1, 2));
        System.out.println(ge(3, 2));
        System.out.println(ge(2, 2));

        System.out.println(ge(2, 2, Integer::compareTo));

        printArray(new Integer[]{1, 2, 3, 4});

        printArray(generateIntArray(10, 1, 20));
    }

}
