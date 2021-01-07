package cn.nihility.alg.datastructure.bst;

import edu.princeton.cs.algs4.Queue;

import java.util.NoSuchElementException;

public class LocalBST<Key extends Comparable<Key>, Value> {

    private class Node {
        private Key key;
        private Value val;
        private Node left, right;
        private int size;

        public Node(Key key, Value value, int size) {
            this.key = key;
            this.val = value;
            this.size = size;
        }

        @Override
        public String toString() {
            return  "{"  + key   +
                    ", " + val +
                    ", " + size  +
                    "}";
        }
    }

    private Node root;

    public LocalBST() { }

    public boolean isEmpty() {
        return 0 == size(root);
    }

    public int size() {
        return size(root);
    }

    private int size(Node x) {
        if (x == null) return 0;
        else return x.size;
    }

    public boolean contains(Key key) {
        if (null == key) throw new IllegalArgumentException("argument to contain() is null");
        return get(key) == null;
    }

    public Value get(Key key) {
        return get(root, key);
    }

    private Value get(Node x, Key key) {
        if (key == null) throw new IllegalArgumentException("argument to get() with a null key");
        if (null == x) return null;
        int comp = key.compareTo(x.key);
        if      (comp > 0) return get(x.right, key);
        else if (comp < 0) return get(x.left, key);
        else               return x.val;
    }

    public void put(Key key, Value val) {
        if (null == key) throw new IllegalArgumentException("argument to put() with a null key");
        if (null == val) {
            delete(key);
            return;
        }
        root = put(root, key, val);
        assert check();
    }

    private Node put(Node x, Key key, Value val) {
        if (null == x) return new Node(key, val, 1);
        int comp = key.compareTo(x.key);
        if      (comp > 0) x.right = put(x.right, key, val);
        else if (comp < 0) x.left  = put(x.left, key, val);
        else               x.val   = val;
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    private boolean check() {
        return true;
    }

    private void delete(Key key) {
        if (null == key) throw new IllegalArgumentException("argument to delete() with a null key");
        root = delete(root, key);
    }

    private Node delete(Node x, Key key) {
        if (x == null) return null;

        int comp = key.compareTo(x.key);
        if      (comp > 0) x.right = delete(x.right, key);
        else if (comp < 0) x.left  = delete(x.left, key);
        else {
            if (null == x.right)  return x.left;
            if (null == x.left)   return x.right;
            Node t = x;
            x = min(t.right);
            x.right = deleteMin(t.right);
            x.left  = t.left;
        }
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    public void deleteMin() {
        if (isEmpty()) throw new NoSuchElementException("symbol table is underflow");
        root = deleteMin(root);
    }

    private Node deleteMin(Node x) {
        if (x.left == null) return x.right;
        x.left = deleteMin(x.left);
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    public Key min() {
        if (isEmpty()) throw new NoSuchElementException("calls min() with empty symbol table");
        return min(root).key;
    }

    private Node min(Node x) {
        if (null == x.left) return x;
        else                return min(x.left);
    }

    public Key max() {
        if (isEmpty()) throw new NoSuchElementException("calls max() with empty symbol table");
        return max(root).key;
    }

    private Node max(Node x) {
        if (x.right == null) return x;
        else                 return max(x.right);
    }

    public void deleteMax() {
        if (isEmpty()) throw new NoSuchElementException("Symbol table underflow");
        root = deleteMax(root);
    }

    private Node deleteMax(Node x) {
        if (x.right == null) return x.left;
        x.right = deleteMax(x.right);
        x.size = 1 + size(x.right) + size(x.left);
        return x;
    }

    public Iterable<Key> keys() {
        if (isEmpty()) return new Queue<>();
        return keys(min(), max());
    }

    public Iterable<Key> keys(Key lo, Key hi) {
        if (lo == null) throw new IllegalArgumentException("first argument to keys() is null");
        if (hi == null) throw new IllegalArgumentException("second argument to keys() is null");

        Queue<Key> keys = new Queue<>();
        keys(root, keys, lo, hi);

        return keys;
    }

    private void keys(Node x, Queue<Key> queue, Key lo, Key hi) {
        if (x == null) return;
        int cmplo = lo.compareTo(x.key);
        int cmphi = hi.compareTo(x.key);
        if (cmplo < 0) keys(x.left, queue, lo, hi);
        if (cmplo <= 0 && cmphi >= 0) queue.enqueue(x.key);
        if (cmphi > 0) keys(x.right, queue, lo, hi);
    }

    public Iterable<Key> levelOrder() {
        Queue<Key> keys = new Queue<>();
        Queue<Node> queue = new Queue<>();
        queue.enqueue(root);
        while (!queue.isEmpty()) {
            Node x = queue.dequeue();
            if (x == null) continue;
            keys.enqueue(x.key);
            queue.enqueue(x.left);
            queue.enqueue(x.right);
        }
        return keys;
    }


    public static void main(String[] args) {
        LocalBST<Integer, Integer> bst = new LocalBST<>();

        bst.put(100, 100);
        bst.put(70, 70);
        bst.put(80, 80);
        bst.put(200, 200);
        bst.put(250, 250);

        Iterable<Integer> it = bst.levelOrder();
        it.forEach(System.out::println);
    }

}
