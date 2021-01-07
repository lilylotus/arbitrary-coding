package cn.nihility.alg.datastructure.avl;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class LocalAVL02<Key extends Comparable<Key>, Value> {

    private class Node {
        private Node left, right;
        private int size;
        private int height;

        private Key key;
        private Value val;

        public Node(int size, int height, Key key, Value val) {
            this.size = size;
            this.height = height;
            this.key = key;
            this.val = val;
        }

        @Override
        public String toString() {
            return "" + key;
        }
    }

    private Node root;

    public LocalAVL02() {
    }

    public int size() {
        return size(root);
    }

    public boolean isEmpty() {
        return root == null;
    }

    private int size(Node x) {
        if (x == null) return 0;
        return x.size;
    }

    public int height() {
        return height(root);
    }

    private int height(Node x) {
        if (x == null) return -1;
        return x.height;
    }


    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("key is null");
        Node node = get(root, key);
        if (null == node) return null;
        return node.val;
    }

    private Node get(Node x, Key key) {
        if (x == null) return null;
        int comp = key.compareTo(x.key);
        if      (comp < 0) return get(x.left, key);
        else if (comp > 0) return get(x.right, key);
        return x;
    }

    public boolean contains(Key key) {
        return get(key) != null;
    }

    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("put() key is null.");
        if (null == val) {
            delete(key);
        } else {
            root = put(root, key, val);
        }
    }

    private Node put(Node x, Key key, Value val) {
        if (x == null) return new Node(1, 0, key, val);
        int comp = key.compareTo(x.key);
        if      (comp > 0) x.right = put(x.right, key, val);
        else if (comp < 0) x.left  = put(x.left, key, val);
        else {             x.val   = val; return x; }
        x.size = 1 + size(x.left) + size(x.right);
        x.height = 1 + Math.max(height(x.left), height(x.right));
        return balance(x);
    }

    private Node balance(Node x) {
        if (balanceFactor(x) < -1) {
            if (balanceFactor(x.right) > 0) {
                x.right = rotateRight(x.right);
            }
            x = rotateLeft(x);
        }
        else if (balanceFactor(x) > 1) {
            if (balanceFactor(x.left) < 0) {
                x.left = rotateLeft(x.left);
            }
            x = rotateRight(x);
        }
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        y.left = x;
        y.size = x.size;
        x.size = 1 + size(x.left) + size(x.right);
        x.height = 1 + Math.max(height(x.left), height(x.right));
        y.height = 1 + Math.max(height(y.left), height(y.right));
        return y;
    }

    private Node rotateRight(Node x) {
        Node y = x.left;
        x.left = y.right;
        y.right = x;
        y.size = x.size;
        x.size = 1 + size(x.left) + size(x.right);
        x.height = 1 + Math.max(height(x.left), height(x.right));
        y.height = 1 + Math.max(height(y.left), height(y.right));
        return y;
    }

    private int balanceFactor(Node x) {
        return height(x.left) - height(x.right);
    }

    private void delete(Key key) {
        if (contains(key)) {
            root = delete(root, key);
        }
    }

    private Node delete(Node x, Key key) {
        if (x == null) return null;
        int comp = key.compareTo(x.key);
        if (comp > 0) x.right = delete(x.right, key);
        else if (comp < 0) x.left = delete(x.left, key);
        else {
            if (x.left == null) return x.right;
            if (x.right == null) return x.left;
            Node t = x;
            x = min(x.right);
            x.left = t.left;
            x.right = deleteMin(x.right);
        }
        x.height = 1 + Math.max(height(x.left), height(x.right));
        x.size = 1 + size(x.left) + size(x.right);
        return balance(x);
    }

    public void deleteMin() {
        if (isEmpty()) throw new NoSuchElementException("Empty symbol table.");
        root = deleteMin(root);
    }

    private Node deleteMin(Node x) {
        if (x.left == null) return x.right;
        x.left = deleteMin(x.left);
        x.height = 1 + Math.max(height(x.left), height(x.right));
        x.size = 1 + size(x.left) + size(x.right);
        return balance(x);
    }

    public Key min() {
        if (isEmpty()) throw new NoSuchElementException("Empty symbol table.");
        Node min = min(root);
        if (null != min) return min.key;
        return null;
    }

    private Node min(Node x) {
        if (x.left != null) return min(x.left);
        return x;
    }

    public Key max() {
        if (isEmpty()) throw new NoSuchElementException("Empty symbol table.");
        Node max = max(root);
        if (null != max) return max.key;
        return null;
    }

    private Node max(Node x) {
        if (x.right != null) return min(x.right);
        return x;
    }

    public void deleteMax() {
        if (isEmpty()) throw new NoSuchElementException("Empty symbol table.");
        root = deleteMax(root);
    }

    private Node deleteMax(Node x) {
        if (x.right == null) return x.left;
        x.right = deleteMin(x.right);
        x.height = 1 + Math.max(height(x.left), height(x.right));
        x.size = 1 + size(x.left) + size(x.right);
        return balance(x);
    }

    public Iterable<Key> keysInOrder() {
        Queue<Key> queue = new LinkedList<>();
        keysInOrder(root, queue);
        return queue;
    }

    private void keysInOrder(Node x, Queue<Key> queue) {
        if (x == null) return;
        keysInOrder(x.left, queue);
        queue.add(x.key);
        keysInOrder(x.right, queue);
    }

    public Iterable<Key> keysLevelOrder() {
        Queue<Node> queue = new LinkedList<>();
        Queue<Key> keys = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            keys.add(node.key);
            if (node.left != null) queue.add(node.left);
            if (node.right != null) queue.add(node.right);
        }
        return keys;
    }

    public static void main(String[] args) {
        LocalAVL02<Integer, Integer> t = new LocalAVL02<>();
//        AVLTreeST<Integer, Integer> t = new AVLTreeST<>();
//        80 : 90 : 100 : 200 : 230 : 250 : 280 : 300 : 350 : 400 : 450 : 500 :
//        300 : 200 : 400 : 90 : 250 : 350 : 500 : 80 : 100 : 230 : 280 : 450 :

        t.put(100, 100);
        t.put(200, 200);
        t.put(300, 300);
        t.put(90, 90);
        t.put(80, 80);
        t.put(250, 250);
        t.put(230, 230);
        t.put(280, 280);
        t.put(400, 400);
        t.put(350, 350);
        t.put(500, 500);
        t.put(450, 450);

        t.keysInOrder().forEach(s -> System.out.print(s + " : "));
        System.out.println();
        t.keysLevelOrder().forEach(s -> System.out.print(s + " : "));
        System.out.println();

    }

}
