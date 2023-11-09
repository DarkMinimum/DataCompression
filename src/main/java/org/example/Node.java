package org.example;

public class Node {
    private int weight;
    private String symbol;
    private Node right;
    private Node left;

    public Node(String symbol, int weight, Node right, Node left) {
        this.weight = weight;
        this.symbol = symbol;
        this.right = right;
        this.left = left;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public String symbol() {
        return this.symbol;
    }

    public int weight() {
        return this.weight;
    }


    public void incrementWeight() {
        this.weight = this.weight + 1;
    }

    public boolean isLeaf() {
        return (this.right == null && this.left == null);
    }

    public boolean isSymbolsFound(String symbol) {
        var result = this.symbol.equals(symbol);
        if (this.isLeaf()) {
            return result;
        } else {
            return right.isSymbolsFound(symbol) || left.isSymbolsFound(symbol);
        }
    }

    public String findPath(String symbol) {
        if (this.symbol.equals(symbol))
            return "";
        else {
            if (this.right.isSymbolsFound(symbol)) {
                return right.findPath(symbol, "1");
            } else if (this.left.isSymbolsFound(symbol)) {
                return left.findPath(symbol, "0");
            }
        }
        return "";
    }

    public String findPath(String symbol, String path) {
        if (this.symbol.equals(symbol))
            return path;
        else {
            if (this.right.isSymbolsFound(symbol)) {
                return right.findPath(symbol, path + "1");
            } else if (this.left.isSymbolsFound(symbol)) {
                return left.findPath(symbol, path + "0");
            }
        }
        return "";
    }
}
