package cn.nihility.alg.datastructure.bst;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class LocalBinarySearchTree<Key extends Comparable<Key>, Value> {

    private class Node {
        private Key key;
        private Value val;

        private int size;
        private Node left, right;

        public Node(Key key, Value val, int size) {
            this.key = key;
            this.val = val;
            this.size = size;
        }
    }

    private Node root;

    public LocalBinarySearchTree() {
    }

    /******************************************************
    * 工具方法
    *******************************************************/

    public int size() {
        if (null == root) return 0;
        else return root.size;
    }

    private int size(Node x) {
        if (x == null) return 0;
        else return x.size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(Key key) {
        if (null == key) throw new IllegalArgumentException("argument to contains() key is null");
        return get(root, key) == null;
    }

    private Value get(Node x, Key key) {
        if (null == x) return null;
        int comp = key.compareTo(x.key);
        if (comp > 0) return get(x.right, key);
        else if (comp < 0) return get(x.left, key);
        else return x.val;
    }


    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("call put() with null key.");
        if (null == val) {
            delete(key);
        } else {
            root = put(root, key, val);
        }
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


    public void delete(Key key) {
        if (key == null) throw new IllegalArgumentException("call delete() with null key.");
        root = delete(root, key);
    }

    private Node delete(Node x, Key key) {
        if (x == null) return null;
        int comp = key.compareTo(x.key);
        if      (comp > 0) x.right = delete(x.right, key);
        else if (comp < 0) x.left  = delete(x.left, key);
        else {
            if (x.left == null) return x.right;
            if (x.right == null) return x.left;
            Node t = x;
            x = min(x.right);
            x.right = deleteMin(t.right);
            x.left = t.left;
        }
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    private Node deleteMin(Node x) {
        if (x.left == null) return x.right;
        x.left = deleteMin(x.left);
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    private Node deleteMax(Node x) {
        if (x.right == null) return x.left;
        x.right = deleteMax(x.right);
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    public Key min() {
        if (isEmpty()) throw new NoSuchElementException("Symbol table no element.");
        return min(root).key;
    }

    private Node min(Node x) {
        //if (x.left != null) return min(x.left);
        // return x;
        while (x.left != null) { x = x.left; }
        return x;
    }

    public Key max() {
        if (isEmpty()) throw new NoSuchElementException("Symbol table no element.");
        return max(root).key;
    }

    private Node max(Node x) {
        if (x.right == null) return x;
        else return max(x.right);
    }

    public Key ceiling(Key key) {
        if (null == key) throw new IllegalArgumentException("call ceiling() key is null");
        if (isEmpty()) throw new NoSuchElementException("calls ceiling() with empty symbol table");
        Node node = ceiling(root, key);
        if (null == node) return null;
        else return node.key;
    }

    private Node ceiling(Node x, Key key) {
        if (x == null) return null;
        int comp = key.compareTo(x.key);
        if (comp == 0) return x;
        else if (comp < 0) {
            Node t = ceiling(x.left, key);
            if (t != null) return t;
            else return x;
        }
        else return ceiling(x.right, key);
    }

    public Key floor(Key key) {
        if (null == key) throw new IllegalArgumentException("call floor() key is null");
        if (isEmpty()) throw new NoSuchElementException("calls ceiling() with empty symbol table");
        Node node = floor(root, key);
        if (null == node) return null;
        else return node.key;
    }

    private Node floor(Node x, Key key) {
        if (x == null) return null;
        int comp = key.compareTo(x.key);
        if (comp == 0) return x;
        else if (comp > 0) {
            Node t = floor(x.right, key);
            if (t != null) return t;
            else return x;
        }
        return floor(x.left, key);
    }

    public Iterable<Key> keys() {
        if (isEmpty()) return new LinkedList<>();
        return keys(min(), max());
    }

    private Iterable<Key> keys(Key lo, Key hi) {
        if (lo == null) throw new IllegalArgumentException("first argument to keys() is null");
        if (hi == null) throw new IllegalArgumentException("second argument to keys() is null");

        Queue<Key> queue = new LinkedList<>();
        keys(root, queue, lo, hi);
        return queue;
    }

    private void keys(Node x, Queue<Key> queue, Key lo, Key hi) {
        if (x == null) return;
        int hcomp = hi.compareTo(x.key);
        int lcomp = lo.compareTo(x.key);

        if (lcomp < 0) keys(x.left, queue, lo, hi);
        if (lcomp <= 0 && hcomp >= 0) queue.add(x.key);
        if (hcomp > 0) keys(x.right, queue, lo, hi);
    }

    public int height() {
        return height(root);
    }

    private int height(Node x) {
        if (x == null) return 0;
        return 1 + Math.max(height(x.left), height(x.right));
    }

    public Iterable<Key> levelOrder() {
        Queue<Key> keys = new LinkedList<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node x = queue.poll();
            if (x == null) continue;
            keys.add(x.key);
            queue.add(x.left);
            queue.add(x.right);
        }
        return keys;
    }

    public static void main(String[] args) {
        LocalBinarySearchTree<Integer, Integer> bst = new LocalBinarySearchTree<>();
        bst.put(100, 100);
        bst.put(50, 50);
        bst.put(40, 40);
        bst.put(70, 70);
        bst.put(200, 200);
        bst.put(180, 180);
        bst.put(250, 250);
        bst.put(240, 240);

        bst.keys().forEach(s -> System.out.print(s + " : "));
        System.out.println();
        bst.levelOrder().forEach(s -> System.out.print(s + " : "));
        System.out.println("Height : " + bst.height());
    }

}
