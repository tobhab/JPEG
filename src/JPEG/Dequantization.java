package JPEG;

import java.io.IOException;

public class Dequantization {
  private int n; // Block size
  private double[][] result;

  /**
   * Reverse the quantization by multiply each block with quantization matrix. The quantization matrix should be in the form of [block size x block size]
   * 
   * @param layer
   * @param quantizationMatrix
   * @throws IOException
   */
  public Dequantization(int[][] layer, int[][] quantizationMatrix, String fileName) throws IOException {
    this.n = quantizationMatrix.length;
    int width = layer[0].length;
    int height = layer.length;

    result = new double[height][width];

    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++)
        result[y][x] = layer[y][x] * quantizationMatrix[y % n][x % n];

    Matrix.toTxt(result, fileName + "_dequantization.txt");
  }

  public double[][] getDequantizationResult() {
    return result;
  }

}
