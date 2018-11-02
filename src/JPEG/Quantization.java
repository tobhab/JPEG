package JPEG;

import static java.lang.Math.round;

import java.io.IOException;

public class Quantization {

  private int n;
  private int[][] result;

  /**
   * Quantize a layer by dividing each block with the quantization matrix.
   * The quantization matrix should be in the form of [block size x block size]
   * 
   * @param layer
   * @param quantizationMatrix
   * @throws IOException
   */
  public Quantization(double[][] layer, int[][] quantizationMatrix, String fileName) throws IOException {
    this.n = quantizationMatrix.length;
    int width = layer[0].length;
    int height = layer.length;

    result = new int[height][width];

    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++)
        result[y][x] = (int) (round(layer[y][x] / (double) (quantizationMatrix[y % n][x % n])));

    Matrix.toTxt(result, fileName + "_quantization.txt");
  }

  public int[][] getQuantizationResult() {
    return result;
  }
  
  public void setQuantizationValue(int y, int x, int value) {
	  this.result[y][x] = value;
  }
  
  public void saveQuantizationMatrix(String fileName) throws IOException {
	  Matrix.toTxt(result, fileName);
  }

}
