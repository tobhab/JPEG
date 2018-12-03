package JPEG;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class CreateHuffmanTree {

  ArrayList<Node> leafs;
  int all;
  Node root;

  /**
   * Build a Huffman table for a given int array
   * 
   * @param arr
   */
  public CreateHuffmanTree(int[] arr) {

    all = arr.length;
    HashMap<Integer, Node> leafMap = new HashMap<Integer, Node>();

    // create map of elements with their probability 
    for (int key : arr)
      if (leafMap.containsKey(key))
        leafMap.get(key).probability++;
      else
        leafMap.put(key, new Node(key));

    leafs = new ArrayList<Node>(leafMap.values());
    leafs.sort(Comparator.comparingInt(X -> X.probability));

    this.root = createTree();
    // this.root = createTree2();
    System.out.println("Huffman code:");
    printTree(root, "");
  }

  /**
   * Create a balanced tree from all elements and their probability
   * 
   * @return root node of tree
   */
  public Node createTree() {
    ArrayList<Node> tmp = new ArrayList<Node>();

    while (leafs.size() > 1) {
      while (leafs.size() > 1) {
        Node leftChild = leafs.remove(0);
        Node rightChild = leafs.remove(0);
        Node newNode = new Node(leftChild, rightChild);
        leftChild.parent = newNode;
        rightChild.parent = newNode;
        root = newNode;
        tmp.add(newNode);
      }
      if (leafs.size() == 1) {
        Node n = leafs.get(0);
        leafs.remove(0);
        tmp.add(n);
      }

      leafs = tmp;
    }

    return root;
  }

  /**
   * Create a inbalanced tree from all elements and their probability (not good)
   * 
   * @return root node of tree
   */
  public Node createTree2() {
    while (leafs.size() > 1) {
      root = new Node(leafs.remove(0), leafs.remove(0));
      leafs.add(0, root);
    }

    return root;
  }

  public void printTree(Node n, String code) {
    if (n.isNode) {
      if (n.left != null)
        printTree(n.left, code + "0");
      if (n.right != null)
        printTree(n.right, code + "1");
      return;
    }
    System.out.printf("Value: %3d/%d Codewort: %s\n", n.value, n.probability, code);
  }

}

/**
 * Node class represent an element with its value and the probability
 */
class Node {
  boolean isNode;
  int value;
  int probability;

  Node left, right, parent;

  public Node(int value) {
    this.value = value;
    probability = 1;
    isNode = false;
  }

  public Node(Node n1, Node n2) {
    left = n1;
    right = n2;
    isNode = true;
    probability = n1.probability + n2.probability;
  }

  public String getCode() {
    if (parent == null) {
      return "";
    }
    return parent.getCode(this);
  }

  private String getCode(Node caller) {
    if (caller == left) {
      return getCode() + "0";
    } else {
      return getCode() + "1";
    }
  }

  /**
   * Add the given node to the left most postition below this node.
   * It tries to traverse the tree on the same level to find a location to add the node
   * @param toAdd
   * @return The parent to which the node was actually added
   */
  public Node addLeftMost(Node toAdd)
  {
    if(left == null)
    {
      left = toAdd;
      return  this;
    }
    if(right == null)
    {
      right = toAdd;
      return traverseRight();
    }
    Node toReturn = traverseRight();
    toReturn.addLeftMost(toAdd);
    return toReturn;
  }

  /**
   * Searches and returns the node which is to the right on the same level in the tree
   * @return The node which is to the right of this node
   */
  private Node traverseRight() {
    int stepsUp = 0;
    Node current = this;
    while(current.right != null)
    {
      if(current.parent == null)
      {
        //using runtime exceptions since they don't need to be added to the method signature
        throw new RuntimeException();
      }
      current = current.parent;
      stepsUp++;
    }

    current.right = new Node(current);
    current = current.right;
    while (stepsUp > 0)
    {
      current.left = new Node(current);
      current = current.left;
      stepsUp--;
    }
    return current;
  }

  /**
   *
   * @return The node which was able to be placed directly below or to the right below this node
   */
  public  Node traverseDown() {
    if(left == null)
    {
      left = new Node(this);
      return left;
    }
    if(right == null)
    {
      right = new Node(this);
      return right;
    }
     return traverseRight().traverseDown();
  }

  public String toString() {
    String s = "";
    if (isNode)
      s += "Node[";
    if (!isNode)
      s += "Leaf[";
    s += "Value: " + value;
    s += " Probability: " + probability;
    s += "]\n";
    return s;
  }

}
