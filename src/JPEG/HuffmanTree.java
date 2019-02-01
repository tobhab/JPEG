package JPEG;

import java.io.IOException;
import java.util.*;

public class HuffmanTree {

  public enum HuffmanTreeType {
    LUMENANCE_AC,
    LUMENANCE_DC,
    CHROMINACE_AC,
    CHROMINACE_DC
  }

  ArrayList<Node> leafs;
  int all;
  Node root;

  /**
   * Build a Huffman table for a given int array
   *
   * @param arr
   */
  public HuffmanTree(int[] arr) {

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
  }

  /**
   * Build a Huffman table for a given lengths and values arrays like in section K in ITU-T81
   *
   * @param lengths The number of values for a given bit width
   * @param values  The values for the bit width
   */
  public HuffmanTree(short[] lengths, short[] values) {
    //The tree is constructed from the top down in this function
    root = new Node(null);
    Node current = root;

    leafs = new ArrayList<Node>();

    int levelNumber = 0;
    int nodesLeftInLevel;
    int indexInValues = 0;

    do {
      nodesLeftInLevel = lengths[levelNumber];
      levelNumber++;

      while (nodesLeftInLevel > 0) {
        Node test = new Node(values[indexInValues++]);
        leafs.add(test);
        nodesLeftInLevel--;
        current = current.addLeftMost(test);
      }
      current = current.traverseDown();

    }
    while (levelNumber < lengths.length);
  }

  /**
   * Create a balanced tree from all elements and their probability
   *
   * @return root node of tree
   */
  private Node createTree() {
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
  private Node createTree2() {
    while (leafs.size() > 1) {
      root = new Node(leafs.remove(0), leafs.remove(0));
      leafs.add(0, root);
    }

    return root;
  }

  /**
   * Compiles an array of how many leafs are per level and the leafs in order.
   * The arrays are of the same format which is saved in the jpeg file.
   * First array at index 0 holds the number of nodes per level
   * Second array at index 1 holds the actual values
   *
   * @return
   */
  public byte[][] getArrays() {
    byte[] lengthPerLevel = new byte[16];

    List<Node> nodes = getInOrder(root);
    List<Node> leafsInORder = new ArrayList<>();
    for (Node node :
            nodes) {
      if (!node.isNode) {
        leafsInORder.add(node);
      }
    }

    byte[] nodeValues = new byte[leafsInORder.size()];
    int nodesSize = leafsInORder.size();
    for (int i = 0; i < nodesSize; i++) {
      int leafDepth = leafsInORder.get(i).getDepth();

      lengthPerLevel[leafDepth]++;
      int valueOfLeaf = leafsInORder.get(i).value;
      nodeValues[i] = (byte) valueOfLeaf;
    }

    return new byte[][]{lengthPerLevel, nodeValues};
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

  /**
   * Returns all the nodes and leafs in the tree as a list in order
   */
  public List<Node> getInOrder(Node root) {
    List<Node> returnList = null;

    if (root.left != null) {
      returnList = getInOrder(root.left);
    }

    if (returnList == null) {
      returnList = new ArrayList<>();
    }
    returnList.add(root);

    if (root.right != null) {
      returnList.addAll(getInOrder(root.right));
    }
    return returnList;
  }

  public void printInOrder(Node n) {
    List<Node> returnList = getInOrder(n);
    for (Node node :
            returnList) {
      if (node.isNode) {
        System.out.println("Node: " + node);
      } else {
        System.out.println("Leaf: " + node);
      }
    }
  }

  /**
   * Returns the value encoded by the first codeword in the binary stream
   */
  public int lookUpCodeNumber(BitStreamReader reader) throws IOException {
    return root.findValueByCode(reader);
  }

  /**
   * Looks up the code in the huffman tree for the value and writes it's coded form into the writer
   */
  public void writeCodeToWriter(BitStreamWriter writer, int valueToWrite) throws IOException {
    for (Node leaf : leafs) {
      if (leaf.value == valueToWrite) {
        CodeWord codeWordToWrite = leaf.getCode();
        writer.write(codeWordToWrite.code, codeWordToWrite.bitCount);
        break;
      }
    }
  }
}

class CodeWord {
  /**
   * holds the bits right aligned and MSB on the left
   */
  public int code = 0;
  public int bitCount = 0;
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
    if (n1 != null && n2 != null) {
      probability = n1.probability + n2.probability;
    }
  }

  public Node(Node parent) {
    this(null, null);
    this.parent = parent;
  }

  public String getCodeAsString() {
    if (parent == null) {
      return "";
    }
    return parent.getCodeAsString(this);
  }

  private String getCodeAsString(Node caller) {
    if (caller == left) {
      return getCodeAsString() + "0";
    } else {
      return getCodeAsString() + "1";
    }
  }

  public CodeWord getCode() {
    CodeWord codeWord = getCode(new CodeWord());
    return codeWord;
  }

  private CodeWord getCode(CodeWord codeWord) {
    if (parent == null) {
      return codeWord;
    }
    return parent.getCode(this, codeWord);
  }

  private CodeWord getCode(Node caller, CodeWord codeWord) {
    if (caller == left) {
      //rightmost bit must be set to zero
      //Nothing to be done since numbers are by default all zero
    } else {
      //rightmost bit must be set to one
      codeWord.code |= 1 << codeWord.bitCount;
    }
    codeWord.bitCount++;

    return getCode(codeWord);
  }

  /**
   * Returns the depth of a node. The root in this case has the depth of -1, 
   * because the first layer below that (depth 0) is the first one where data
   * can be stored without being a trivial huffman tree with only one stored value.
   */
  public int getDepth()
  {
    if(parent == null)
    {
      return -1;
    }
    else
    {
      return parent.getDepth() + 1;
    }
  }

  /**
   * Add the given node to the left most postition below this node.
   * It tries to traverse the tree on the same level to find a location to add the node
   *
   * @param toAdd
   * @return The parent to which the node was actually added
   */
  public Node addLeftMost(Node toAdd) {
    if (left == null) {
      left = toAdd;
      left.parent = this;
      return this;
    }
    if (right == null) {
      right = toAdd;
      right.parent = this;
      return traverseRight();
    }
    Node toReturn = traverseRight();
    toReturn.addLeftMost(toAdd);
    return toReturn;
  }

  /**
   * Creates the necessary nodes so that the returned node lies to the right of this one.
   *
   * @return The node which is to the right of this node
   */
  private Node traverseRight() {
    int stepsUp = 0;
    Node current = this;
    while (current.right != null) {
      if (current.parent == null) {
        //using runtime exceptions since they don't need to be added to the method signature
        //This also should never be used except when someone tries to edit this function.
        throw new RuntimeException();
      }
      current = current.parent;
      stepsUp++;
    }

    current.right = new Node(current);
    current = current.right;
    while (--stepsUp > 0) {
      current.left = new Node(current);
      current = current.left;
    }
    return current;
  }

  /**
   * Creates the necessary nodes so that the returned node lies below this node
   * @return The node which was able to be placed directly below or to the right below this node
   */
  public Node traverseDown() {
    if (left == null) {
      left = new Node(this);
      return left;
    }
    if (right == null) {
      right = new Node(this);
      return right;
    }
	//No free nodes under this one, move the right and find an empty spot there
    return traverseRight().traverseDown();
  }

  /**
   * Looks up which value corresponds to the code which was written first in the reader.
   */
  public int findValueByCode(BitStreamReader reader) throws IOException {
    if (!isNode) //is a leaf
    {
      return value;
    }
    boolean nextBit = reader.readBit();
    if (nextBit) {
      return right.findValueByCode(reader);
    } else {
      return left.findValueByCode(reader);
    }
  }

  public String toString() {
    String s = "";
    if (isNode)
      s += "Node[";
    else
      s += "Leaf[";
    s += "Value: " + value;
    s += " Probability: " + probability;
    s += "]\n";
    return s;
  }

}
