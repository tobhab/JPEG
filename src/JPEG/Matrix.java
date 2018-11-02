package JPEG;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;

/**
 * The Matrix class provides basic matrix operations (add, minus, multiply and prints for testing)
 */
public class Matrix {

  final static int BLOCK_SIZE = 8;

  public static double[][] mult(double[][] a, double[][] b) {
    double[][] result = new double[a.length][b[0].length];

    for (int i = 0; i < a.length; i++)
      for (int j = 0; j < b[0].length; j++)
        for (int k = 0; k < a[0].length; k++)
          result[i][j] = result[i][j] + a[i][k] * b[k][j];

    return result;
  }

  public static double[][] mult(double[] a, double[][] b) {
    return mult(toMatrix(a), b);
  }

  public static double[][] mult(double[][] a, double[] b) {
    return mult(a, toMatrix(b));
  }

  public static double[][] mult(double[][] a, int[][] b) {
    return mult(a, toMatrix(b));
  }

  public static double[][] mult(int[][] a, double[][] b) {
    return mult(toMatrix(a), b);
  }

  public static double[][] multi(int[] a, double[][] b) {
    return mult(toMatrix(a), b);
  }

  public static double[][] mult(int[][] a, int[][] b) {
    return mult(toMatrix(a), toMatrix(b));
  }

  public static double[][] mult(int[] a, double[][] b) {
    return mult(toMatrix(a), b);
  }

  public static double[][] mult(int[] a, int[][] b) {
    return mult(toMatrix(a), b);
  }

  public static double[][] mult(int[][] a, int[] b) {
    return mult(a, toMatrix(b));
  }

  public static double[][] add(double[][] a, double[][] b) {
    double[][] result = new double[a.length][b[0].length];

    for (int i = 0; i < a.length; i++)
      for (int j = 0; j < b[0].length; j++)
        result[i][j] = a[i][j] + b[i][j];

    return result;
  }

  public static double[][] minus(double[][] a, double[][] b) {
    double[][] result = new double[a.length][b[0].length];

    for (int i = 0; i < a.length; i++)
      for (int j = 0; j < b[0].length; j++)
        result[i][j] = a[i][j] - b[i][j];

    return result;
  }

  public static double[][] add(double[] a, double[][] b) {
    return add(toMatrix(a), b);
  }

  public static double[][] add(double[][] a, double[] b) {
    return add(a, toMatrix(b));
  }

  public static double[][] add(double[] a, double[] b) {
    return add(toMatrix(a), toMatrix(b));
  }

  public static double[][] add(int[][] a, double[][] b) {
    return add(toMatrix(a), b);
  }

  public static double[][] add(int[] a, double[][] b) {
    return add(toMatrix(a), b);
  }

  public static double[][] add(double[][] a, int[][] b) {
    return add(a, toMatrix(b));
  }

  public static double[][] add(double[][] a, int[] b) {
    return add(a, toMatrix(b));
  }

  public static double[][] toMatrix(int[] m) {
    return toMatrix(new int[][] { m });
  }

  public static double[][] toMatrix(int[][] m) {
    double[][] result = new double[m.length][m[0].length];
    for (int i = 0; i < m.length; i++)
      for (int j = 0; j < m[0].length; j++)
        result[i][j] = m[i][j];

    return result;
  }

  public static double[][] toMatrix(double[] m) {
    return new double[][] { m };
  }

  public static void print(int[][][] arr, int counter) {
    for (int x = 0; x < arr.length; x++) {
      for (int y = 0; y < arr[0].length; y++) {
        System.out.printf("[%d,%d,%d] ", arr[x][y][0], arr[x][y][1], arr[x][y][2]);
      }
      if (counter-- == 0) {
        System.out.println();
        break;
      }
      System.out.println();
    }
  }

  public static void print(int[][] arr, int counter) {
    for (int x = 0; x < arr.length; x++) {
      System.out.printf("[");
      for (int y = 0; y < arr[0].length; y++) {
        System.out.printf("%4d ", arr[x][y]);
      }
      if (counter-- == 0) {
        System.out.println();
        break;
      }

      System.out.println("]");
    }
  }

  public static void print(int[][] arr, int x_start, int y_start, int x_end, int y_end) {
    for (int x = x_start; x < x_end; x++) {
      System.out.printf("[");
      for (int y = y_start; y < y_end; y++) {
        System.out.printf("%4d ", arr[x][y]);
      }
      System.out.println("]");
    }
  }

  public static void print(double[][] arr, int counter) {
    for (int x = 0; x < arr.length; x++) {
      System.out.print("[");
      for (int y = 0; y < arr[0].length; y++) {
        System.out.printf("%f ", arr[x][y]);
      }
      if (counter-- == 0) {
        System.out.println();
        break;
      }
      System.out.print("]\n");
    }
  }

  public static void toTxt(double[][] arr, String filePath) throws IOException {
    Formatter fmt = new Formatter(new File(filePath));

    for (int y = 0; y < arr.length; y++) {
      for (int x = 0; x < arr[y].length; x++)
        fmt.format("%6.1f ", arr[y][x]);
      fmt.format("\n");
    }

    fmt.flush();
    fmt.close();
  }

  public static void toTxt(int[][] arr, String filePath) throws IOException {
    Formatter fmt = new Formatter(new File(filePath));

    for (int y = 0; y < arr.length; y++) {
      for (int x = 0; x < arr[y].length; x++)
        fmt.format("%5d ", arr[y][x]);
      fmt.format("\n");
    }

    fmt.flush();
    fmt.close();
  }

  
  public static void printBlocks(double[][] arr, int blocks) {
    for (int x = 0; x < blocks * BLOCK_SIZE; x++) {
      System.out.printf("y:%2d [", x);
      for (int y = 0; y < blocks * BLOCK_SIZE; y++) {
        System.out.printf(" %6.1f ", arr[x][y]);
      }
      System.out.print("]\n");
    }
  }

  public static void printBlocks(int[][] arr, int blocks) {
    for (int x = 0; x < blocks * BLOCK_SIZE; x++) {
      System.out.printf("[", x);
      for (int y = 0; y < blocks * BLOCK_SIZE; y++) {
        System.out.printf(" %5d ", arr[x][y]);
      }
      System.out.print("]\n");
    }
  }

}
