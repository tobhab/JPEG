package JPEG;

import static JPEG.Constants.*;

public class RGBConverter {

  private final int[][][] rgbPicture;

  /**
   * Convert a YCbCr array to a RGB array by subtract the yPbPrMatrix and multiply by RGBMatrix.
   * Additionally map the values to a valid range [0..255]
   * 
   * @param yCbCrArray
   * @param RGBMatrix
   * @param yPbPrMatrix
   */
  public RGBConverter(double[][][] yCbCrArray, double[][] RGBMatrix, double[][] yPbPrMatrix) {
    rgbPicture = convertYCbCrtoRGB(yCbCrArray, RGBMatrix, yPbPrMatrix);
  }

  private int[][][] convertYCbCrtoRGB(double[][][] yCbCrArray, double[][] RGBMatrix, double[][] yPbPrMatrix) {

    int height = yCbCrArray.length;
    int width = yCbCrArray[0].length;

    int[][][] rgbPicture = new int[height][width][3];

    // for each YCbCr pixel
    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++) {

        // convert YCbCr to a single vector for calculating
        double[][] yCbCrVector = new double[][] {
            { yCbCrArray[x][y][Y] },
            { yCbCrArray[x][y][Cb] },
            { yCbCrArray[x][y][Cr] },
        };

        double[][] yPbPr = Matrix.minus(yCbCrVector, yPbPrMatrix);
        double[][] rgb = Matrix.mult(RGBMatrix, yPbPr);

        // map and set RGB value from vector
        rgbPicture[x][y][R] = map(rgb[R][0]);
        rgbPicture[x][y][G] = map(rgb[G][0]);
        rgbPicture[x][y][B] = map(rgb[B][0]);
      }
    return rgbPicture;
  }

  private int map(double val) {
    if (val > 255)
      return 255;
    if (val < 0)
      return 0;
    return (int) val;
  }

  public final int[][][] getRGBArray() {
    return rgbPicture;
  }
}
