package cn.nihility.alg.datastructure.avl;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class LocalAVL<Key extends Comparable<Key>, Value> {

    private class Node {
        private final Key key;
        private Value val;
        /* 没有节点为 -1, 有一个节点 0 */
        private int height;
        private int size;
        private Node left;
        private Node right;

        public Node(Key key, Value val, int height, int size) {
            this.key = key;
            this.val = val;
            this.height = height;
            this.size = size;
        }
    }

    private Node root;

    public LocalAVL() {
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return size(root);
    }

    private int size(Node x) {
        if (x == null) return 0;
        return x.size;
    }

    public int height() {
        return height(root);
    }

    private int height(Node x) {
        if (null == x) return -1;
        return x.height;
    }

    public Value get(Key key) {
        if (null == key) throw new IllegalArgumentException("argument to get() key can not be null");
        Node node = get(root, key);
        if (null == node) return null;
        return node.val;
    }

    private Node get(Node x, Key key) {
        if (x == null) return null;
        int comp = key.compareTo(x.key);
        if (comp > 0) return get(x.right, key);
        else if (comp < 0) return get(x.left, key);
        else return x;
    }

    public boolean contains(Key key) {
        if (null == key) throw new IllegalArgumentException("argument to contains() key can not be null");
        return get(key) != null;
    }

    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("first argument to put() is null");
        if (val == null) {
            delete(key);
            return;
        }
        root = put(root, key, val);
    }

    private Node put(Node x, Key key, Value val) {
        if (x == null) return new Node(key, val, 0, 1);
        int comp = key.compareTo(x.key);
        if (comp > 0) x.right = put(x.right, key, val);
        else if (comp < 0) x.left = put(x.left, key, val);
        else {
            x.val = val;
            return x;
        }
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
        } else if (balanceFactor(x) > 1) {
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
        y.height = 1 + Math.max(height(y.left), height(y.right));
        x.height = 1 + Math.max(height(x.left), height(x.right));
        return y;
    }

    private Node rotateRight(Node x) {
        Node y = x.left;
        x.left = y.right;
        y.left = x;
        y.size = x.size;
        x.size = 1 + size(x.left) + size(x.right);
        x.height = 1 + height(x.left) + height(x.right);
        y.height = 1 + height(y.left) + height(y.right);
        return y;
    }

    private int balanceFactor(Node x) {
        return height(x.left) - height(x.right);
    }

    private void delete(Key key) {
        if (null == key) throw new IllegalArgumentException("argument to delete() key can not be null.");
        if (!contains(key)) return;
        root = delete(root, key);
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
            x = min(t.right);
            x.left = t.left;
            x.right = deleteMin(t.right);
        }
        x.size = 1 + size(x.right) + size(x.left);
        x.height = 1 + height(x.left) + height(x.right);
        return balance(x);
    }

    private Node min(Node x) {
        while (null != x && x.left != null) {
            x = x.left;
        }
        return x;
    }

    public Key min() {
        if (isEmpty()) throw new NoSuchElementException("called min() with empty symbol table");
        return min(root).key;
    }

    private Node max(Node x) {
        while (x != null && x.right != null) {
            x = x.right;
        }
        return x;
    }

    public Key max() {
        if (isEmpty()) throw new NoSuchElementException("called max() with empty symbol table.");
        return max(root).key;
    }

    private Node deleteMin(Node x) {
        if (x.left != null) return x.right;
        x.left = deleteMin(x.left);
        x.size = 1 + size(x.left) + size(x.right);
        x.height = 1 + Math.max(height(x.left), height(x.right));
        return balance(x);
    }

    public void deleteMin() {
        if (isEmpty()) throw new NoSuchElementException("called deleteMin() with empty symbol table.");
        root = deleteMin(root);
    }

    private Node deleteMax(Node x) {
        if (x.right == null) return x.left;
        x.right = deleteMax(x.right);
        x.size = 1 + size(x.left) + size(x.right);
        x.height = 1 + Math.max(height(x.left), height(x.right));
        return balance(x);
    }

    public void deleteMax() {
        if (isEmpty()) throw new NoSuchElementException("called deleteMax() with empty symbol table.");
        root = deleteMax(root);
    }

    public Iterable<Key> keys() {
        return keysInOrder();
    }

    private Iterable<Key> keysInOrder() {
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
        Queue<Key> queue = new LinkedList<>();
        if (!isEmpty()) {
            Queue<Node> queue2 = new LinkedList<>();
            queue2.add(root);
            while (!queue2.isEmpty()) {
                Node x = queue2.poll();
                queue.add(x.key);
                if (x.left != null) {
                    queue2.add(x.left);
                }
                if (x.right != null) {
                    queue2.add(x.right);
                }
            }
        }
        return queue;
    }

}
