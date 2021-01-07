package cn.nihility.other;

import java.util.Random;

public class OtherUtil {

    public static int[] randIntArray(int len, int min, int max) {
        int[] array = new int[len];
        Random random = new Random(System.currentTimeMillis());
        int range = max - min + 1;
        for (int i = 0; i < len; i++) {
            array[i] = random.nextInt(range) + min;
        }
        return array;
    }

    public static void print(int[] array) {
        int last = array.length - 1;
        for (int i = 0; i < last; i++) {
            System.out.print(array[i] + " : ");
        }
        System.out.println(array[last]);
    }

    public static void swap(int[] array, int x, int y) {
        int tmp = array[x];
        array[x] = array[y];
        array[y] = tmp;
    }

}
