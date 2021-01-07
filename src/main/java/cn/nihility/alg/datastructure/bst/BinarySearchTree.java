package cn.nihility.alg.datastructure.bst;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Binary Search Tree (BST)
 * @param <T> Any comparable data is allowed within this tree (numbers, strings, comparable Objects, etc...)
 */
public class BinarySearchTree<T extends Comparable<T>> {

    // Tracks the number of nodes in this BST
    private int nodeCount = 0;

    // This BST is a rooted tree so we maintain a handle on the root node
    private Node root = null;

    // Internal node containing node references and the actual node data
    private class Node {
        T data;
        Node left, right;

        public Node(Node left, Node right, T elem) {
            this.data = elem;
            this.left = left;
            this.right = right;
        }
    }

    // Check if this binary tree is empty
    public boolean isEmpty() {
        return size() == 0;
    }

    // Get the number of nodes in this binary tree
    public int size() {
        return nodeCount;
    }

    // Add an element to this binary tree. Returns true
    // if we successfully perform an insertion
    public boolean add(T elem) {
        // Check if the value already exists in this
        // binary tree, if it does ignore adding it
        if (contains(elem)) {
            return false;
        } else {
            root = add(root, elem);
            nodeCount++;
            return true;
        }
    }

    private Node add(Node node, T elem) {
        if (node == null) {
            node = new Node(null, null, elem);
        } else {
            if (elem.compareTo(node.data) > 0) {
                node.right = add(node.right, elem);
            } else {
                node.left = add(node.left, elem);
            }
        }
        return node;
    }

    public boolean contains(T elem) {
        return contains(root, elem);
    }

    private boolean contains(Node node, T elem) {
        if (null == node) { return false; }
        if (elem.compareTo(node.data) > 0) {
            return contains(node.right, elem);
        } else if (elem.compareTo(node.data) < 0) {
            return contains(node.left, elem);
        } else {
            return true;
        }
    }

    // Remove a value from this binary tree if it exists, O(n)
    public boolean remove(T elem) {
        // Make sure the node we want to remove actually exists before we remove it
        if (contains(elem)) {
            root = remove(root, elem);
            nodeCount--;
            return true;
        }
        return false;
    }

    private Node remove(Node node, T elem) {
        if (null == node) { return null; }
        int comp = elem.compareTo(node.data);

        // Dig into left subtree, the value we are looking for is smaller than current value.
        if (comp < 0) {
            node.left = remove(node.left, elem);
        } else if (comp > 0) {
            // Dig into right subtree, the value we are looking for is greater than current value.
            node.right = remove(node.right, elem);
        } else {
            // found the value we wish to remove

            // This is the case only a left subtree or no subtree at all.
            // In this situation just swap the node we wish to remove with its left child.
            if (node.right == null) {
                return node.left;
            } else if (node.left == null) {
                return node.right;
            } else {
                Node min = findMin(node.right);
                node.data = min.data;
                node.right = remove(node.right, node.data);

                // Node max = findMax(node.left);
                // node.data = max.data;
                // node.left = remove(node.left, node.data);
            }
        }
        return node;
    }

    private Node findMin(Node node) {
        if (node == null) { throw new IllegalArgumentException("argument for findMin() node can not be null."); }

        while (node.left != null) { node = node.left; }
        return node;
    }

    public Node findMax(Node node) {
        if (node == null) { throw new IllegalArgumentException("argument for findMax() node can not be null."); }
        while (node.right != null) { node = node.right; }
        return node;
    }

    public int height() {
        return height(root);
    }

    private int height(Node node) {
        if (node == null) { return 0; }
        return Math.max(height(node.left), height(node.right)) + 1;
    }

    public Iterator<T> traversal(TreeTraversalOrder order) {
        switch (order) {
            case PRE_ORDER:
                return preOrderTraversal();
            case IN_ORDER:
                return inOrderTraversal();
            case POST_ORDER:
                return postOrderTraversal();
            case LEVEL_ORDER:
                return levelOrderTraversal();
            default:
                return null;
        }
    }

    private Iterator<T> levelOrderTraversal() {
        final int expectedNodeCount = nodeCount;
        final Queue<Node> queue = new LinkedList<>();
        queue.offer(root);

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                if (expectedNodeCount != nodeCount) throw new java.util.ConcurrentModificationException();
                return root != null && !queue.isEmpty();
            }

            @Override
            public T next() {
                if (expectedNodeCount != nodeCount) throw new java.util.ConcurrentModificationException();
                Node node = queue.poll();
                if (node.left != null) queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
                return node.data;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Iterator<T> postOrderTraversal() {
        final int expectedNodeCount = nodeCount;
        final Stack<Node> stack1 = new Stack<>();
        final Stack<Node> stack2 = new Stack<>();
        stack1.push(root);

        while (!stack1.isEmpty()) {
            Node node = stack1.pop();
            if (null != node) {
                stack2.push(node);
                if (node.left != null) { stack1.push(node.left); }
                if (node.right != null) { stack1.push(node.right); }
            }
        }

        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                if (expectedNodeCount != nodeCount) throw new java.util.ConcurrentModificationException();
                return root != null && !stack2.isEmpty();
            }

            @Override
            public T next() {
                if (expectedNodeCount != nodeCount) throw new java.util.ConcurrentModificationException();
                return stack2.pop().data;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Iterator<T> inOrderTraversal() {
        final int expectedNodeCount = nodeCount;
        final Stack<Node> stack = new Stack<>();
        stack.push(root);

        return new Iterator<T>() {
            Node trav = root;
            @Override
            public boolean hasNext() {
                if (expectedNodeCount != nodeCount) throw new java.util.ConcurrentModificationException();
                return root != null && !stack.isEmpty();
            }

            @Override
            public T next() {
                if (expectedNodeCount != nodeCount) throw new java.util.ConcurrentModificationException();

                while (trav != null && trav.left != null) {
                    stack.push(trav.left);
                    trav = trav.left;
                }

                Node node = stack.pop();

                if (node.right != null) {
                    stack.push(node.right);
                    trav = node.right;
                }

                return node.data;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Iterator<T> preOrderTraversal() {
        final int expectedNodeCount = nodeCount;
        final Stack<Node> stack = new Stack<>();
        stack.push(root);

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                if (expectedNodeCount != nodeCount) throw new java.util.ConcurrentModificationException();
                return root != null && !stack.isEmpty();
            }

            @Override
            public T next() {
                if (expectedNodeCount != nodeCount) throw new java.util.ConcurrentModificationException();
                Node node = stack.pop();
                if (node.right != null) stack.push(node.right);
                if (node.left != null) stack.push(node.left);
                return node.data;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static void main(String[] args) {
        BinarySearchTree<Integer> bst = new BinarySearchTree<>();

        bst.add(100);
        bst.add(200);
        bst.add(150);
        bst.add(250);
        bst.add(50);
        bst.add(30);
        bst.add(80);
        bst.add(10);

        int height = bst.height();
        System.out.println("height " + height);

        Iterator<Integer> pri = bst.preOrderTraversal();
        Iterator<Integer> ini = bst.inOrderTraversal();
        Iterator<Integer> poi = bst.postOrderTraversal();
        Iterator<Integer> lvi = bst.levelOrderTraversal();

        trav(pri);
        trav(ini);
        trav(poi);
        trav(lvi);
    }

    private static <T> void trav(Iterator<T> it){
        it.forEachRemaining(i -> System.out.print(i + " : "));
        System.out.println("\n========================");
    }

}
