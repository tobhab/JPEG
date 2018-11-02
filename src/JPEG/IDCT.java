package JPEG;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

import java.io.IOException;

public class IDCT {

  private int N;
  double[][] result;
  double n1;
  double n2;


  /**
   * Perform the Inverse Discrete Cosine Transform for a given array specified by the block size
   * @param F
   * @param block_size
   * @param fileName
   * @throws IOException
   */
  public IDCT(double[][] F, int block_size, String fileName) throws IOException {
    this.N = block_size;
    int width = F[0].length;
    int height = F.length;

    n1 = sqrt(1.0 / N);
    n2 = sqrt(2.0 / N);

    result = new double[height][width];

    // for each block of image
    for (int y_block = 0; y_block < height; y_block += N)
      for (int x_block = 0; x_block < width; x_block += N)
        idct(F, y_block, x_block, result);

    Matrix.toTxt(result, fileName + "_idct.txt");
  }

  private void idct(double[][] F, int y_block, int x_block, double[][] f) {
    // for each element in block
    for (int x = y_block; x < y_block + N; x++)
      for (int y = x_block; y < x_block + N; y++) {

        // calculate idct (over block)
        double sum = 0;
        for (int m = y_block; m < y_block + N; m++) {
          double Cy = (m == y_block) ? n1 : n2;
          for (int n = x_block; n < x_block + N; n++) {
            double Cx = (n == x_block) ? n1 : n2;
            sum += (Cx * Cy) * F[m][n] *
                cos((2.0 * (x - y_block) + 1.0) * (m - y_block) * PI / (2.0 * N)) *
                cos((2.0 * (y - x_block) + 1.0) * (n - x_block) * PI / (2.0 * N));
          }
        }
        f[x][y] = sum;
      }

  }

  public double[][] getIDCTResult() {
    return result;
  }
}
