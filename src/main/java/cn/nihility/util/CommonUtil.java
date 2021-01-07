package cn.nihility.util;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonUtil {

    public static void printBinary(int n) {
        for (int i = 1; i <= 32; i++) {
            System.out.print((n >>> (32 - i)) & 1);
            if (i % 4 == 0) { System.out.print(' '); }
        }
        System.out.println();
    }

    public static <T> void swap(T[] array, int x, int y) {
        int len = array.length;
        if (x >= len || y >= len || x < 0 || y < 0) return;

        T tmp = array[x];
        array[x] = array[y];
        array[y] = tmp;
    }

    public static <T extends Comparable<T>> boolean gt(T x, T y) {
        if (null == x) throw new IllegalArgumentException("argument to gt() first arg is null");
        return x.compareTo(y) > 0;
    }

    public static <T extends Comparable<T>> boolean ge(T x, T y) {
        if (null == x) throw new IllegalArgumentException("argument to ge() first arg is null");
        return x.compareTo(y) >= 0;
    }

    public static <T extends Comparable<T>> boolean lt(T x, T y) {
        if (null == x) throw new IllegalArgumentException("argument to lt() first arg is null");
        return x.compareTo(y) < 0;
    }

    public static <T extends Comparable<T>> boolean le(T x, T y) {
        if (null == x) throw new IllegalArgumentException("argument to le() first arg is null");
        return x.compareTo(y) <= 0;
    }

    public static <T> void print(T[] array) {
        if (null == array) return;
        System.out.println(Stream.of(array).map(Object::toString).collect(Collectors.joining(" ")));
    }

    public static Integer[] randArray(int len, int min, int max) {
        Random random = new Random(System.currentTimeMillis());
        Integer[] array = new Integer[len];
        int range = max - min + 1;
        for (int i = 0; i < len; i++) {
            array[i] = random.nextInt(range) + min;
        }
        return array;
    }

    public static SortEntity<Integer>[] rangSortArray(int len, int min, int max) {
        Random random = new Random(System.currentTimeMillis());
        SortEntity<Integer>[] array = new SortEntity[len];
        int range = max - min + 1;
        for (int i = 0; i < len; i++) {
            SortEntity<Integer> entity = new SortEntity<>();
            entity.setIndex(i);
            entity.setVal(random.nextInt(range) + min);
            array[i] = entity;
        }
        return array;
    }

    public static void sortArray(ISort<SortEntity<Integer>> sort) {
        SortEntity<Integer>[] array = rangSortArray(20, 10, 100);
        print(array);
        sort.sort(array);
        print(array);
    }

    public static void sort(ISort<Integer> sort) {
        Integer[] array = randArray(20, 10, 200);
        print(array);
        sort.sort(array);
        print(array);
    }

    public static void main(String[] args) {
        print(randArray(10, 10, 100));
    }

}
