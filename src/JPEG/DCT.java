package JPEG;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

import java.io.IOException;

public class DCT {

  private int N;
  double[][] result;
  double n1;
  double n2;

  /**
   * Perform the descrete cosine transform for a given array specified by a block size
   * @param f input array
   * @param block_size
   * @throws IOException 
   */
  public DCT(double[][] f, int block_size, String fileName) throws IOException {
    this.N = block_size;
    int width = f[0].length;
    int height = f.length;

    n1 = sqrt(1.0 / N); 
    n2 = sqrt(2.0 / N);

    result = new double[height][width];

    // For each block of image (block building)
    for (int y_block = 0; y_block < height; y_block += N)
      for (int x_block = 0; x_block < width; x_block += N)
        dct(f, y_block, x_block, result);
    
    Matrix.toTxt(result, fileName +"_dct.txt");
  }

  private void dct(double[][] f, int y_block, int x_block, double[][] F) {
    // For each element in block
    for (int y = y_block; y < y_block + N; y++) {
      double Cy = (y == y_block) ? n1 : n2;
      for (int x = x_block; x < x_block + N; x++) {
        double Cx = (x == x_block) ? n1 : n2;

        // Calculate DCT (over block)
        double sum = 0.0;
        for (int m = y_block; m < y_block + N; m++)
          for (int n = x_block; n < x_block + N; n++)
            sum += f[m][n] *
                cos(((2.0 * (m - y_block) + 1.0) / (2.0 * N)) * (y - y_block) * PI) *
                cos(((2.0 * (n - x_block) + 1.0) / (2.0 * N)) * (x - x_block) * PI);

        F[y][x] = sum * Cy * Cx;
      }
    }
  }

  public double[][] getDCTResult() {
    return result;
  }
}
