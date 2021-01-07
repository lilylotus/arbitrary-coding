package cn.nihility.exec;

import java.util.HashMap;

public class LocalHashMap {

    private static final int MAXIMUM_CAPACITY = 1 << 30;


    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>(16);

        String put = map.put("one", "one");
        System.out.println("put [" + put + "]");
        put = map.put("one1", "one1");
        System.out.println("put [" + put + "]");
        put = map.put("one1", "one2");
        System.out.println("put [" + put + "]");

        hash("one");

        System.out.println("one1".hashCode());
        System.out.println("one2".hashCode());
        System.out.println("one3".hashCode());

        System.out.println(map);

        printBinary(0x7fffffff);

        tableSizeFor(8);
    }

    static int hash(Object obj) {
        int hashCode = obj.hashCode();
        int code2 = hashCode >>> 16;
        int result = hashCode ^ code2;
        System.out.println("--- hash ---");
        printBinary(hashCode);
        printBinary(code2);
        printBinary(result);
        return result;
    }

    static void printBinary(int n) {
        for (int i = 1; i <= 32; i++) {
            System.out.print((n >>> (32 - i)) & 1);
            if (i % 4 == 0) { System.out.print(' '); }
        }
        System.out.println();
    }

    /**
     * Returns a power of two size for the given target capacity.
     */
    static final int tableSizeFor(int cap) {
        printBinary(cap);
        int n = cap - 1;
        printBinary(n);
        n |= n >>> 1;
        printBinary(n);
        n |= n >>> 2;
        printBinary(n);
        n |= n >>> 4;
        printBinary(n);
        n |= n >>> 8;
        printBinary(n);
        n |= n >>> 16;
        printBinary(n);
        int tmp = (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        printBinary(tmp);
        System.out.println("final value [" + tmp + "]");
        return tmp;
    }

}
