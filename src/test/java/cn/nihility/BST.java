package cn.nihility;

import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class BST {

    public static void main(String[] args) {
        edu.princeton.cs.algs4.BST<String, Integer> st = new edu.princeton.cs.algs4.BST<String, Integer>();
        for (int i = 0; !StdIn.isEmpty(); i++) {
            String key = StdIn.readString();
            if ((st.size() > 1) && (st.floor(key) != st.floor2(key)))
                throw new RuntimeException("floor() function inconsistent");
            st.put(key, i);
        }

        for (String s : st.levelOrder())
            StdOut.println(s + " " + st.get(s));

        StdOut.println();

        for (String s : st.keys())
            StdOut.println(s + " " + st.get(s));
    }

}
