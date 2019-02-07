package JPEG;

import static JPEG.Constants.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class YCbCrConverter {

  private final double[][][] yCbCrPicture;

  /**
   * Convert a RGB array to a YCbCr array by multiply the yPbPrMatrix and and add the yCbCrMatrix.
   * 
   * @param rgbArray
   * @param yPbPrMatrix
   * @param yCbCrMatrix
   * @throws IOException
   */
  public YCbCrConverter(int[][][] rgbArray, double[][] yPbPrMatrix, double[][] yCbCrMatrix) throws IOException {
    yCbCrPicture = convertRGBtoYCbCr(rgbArray, yPbPrMatrix, yCbCrMatrix);
    writeImages("layer");
  }

  private double[][][] convertRGBtoYCbCr(int[][][] rgbArray, double[][] yPbPrMatrix, double[][] yCbCrMatrix) {

    int height = rgbArray.length;
    int width = rgbArray[0].length;

    double[][][] yCbCrPicture = new double[height][width][3];

    // for each RGB pixel
    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++) {

        // convert RGB to a single vector for calculating
        int[][] RGBVector = new int[][] {
            { rgbArray[x][y][R] },
            { rgbArray[x][y][G] },
            { rgbArray[x][y][B] }
        };

 /*       // optimization
        double red = rgbArray[x][y][R] / 255.0;
        double green = rgbArray[x][y][G] / 255.0;
        double blue = rgbArray[x][y][B] / 255.0;
        double EY =  (0.299 * red + 0.587 * green + 0.114 * blue);
        double ECb = (-0.1687 * red - 0.3313 * green + 0.5 * blue);
        double ECr = (0.5 * red - 0.4187 * green - 0.0813 * blue);
        yCbCrPicture[x][y][Y] = 219 * EY + 16;
        yCbCrPicture[x][y][Cb] = 224 * ECb + 128;
        yCbCrPicture[x][y][Cr] = 224 * ECr + 128;
*/
        double[][] YPBPR = Matrix.mult(yPbPrMatrix, RGBVector);
        double[][] YCBCR = Matrix.add(YPBPR, yCbCrMatrix);

        yCbCrPicture[x][y][Y] = YCBCR[Y][0];
        yCbCrPicture[x][y][Cb] = YCBCR[Cb][0];
        yCbCrPicture[x][y][Cr] = YCBCR[Cr][0];
      }
    return yCbCrPicture;
  }

  public final double[][][] getYCbCrArray() {
    return yCbCrPicture;
  }

  public void writeImages(String filePath) throws IOException {
    if (!JPEG.writeDebugFiles)
      return;
    int height = yCbCrPicture.length;
    int width = yCbCrPicture[0].length;

    BufferedImage biY = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    BufferedImage biCb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    BufferedImage biCr = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < height; y++)
      for (int x = 0; x < width; x++) {

        // Create Y image
        int r = (int) (yCbCrPicture[y][x][Y]);
        int g = (int) (yCbCrPicture[y][x][Y]);
        int b = (int) (yCbCrPicture[y][x][Y]);

        biY.setRGB(x, y, (r << 16) | (g << 8) | b);

        // Create Cb image
        r = 0;
        g = (int) (255 - yCbCrPicture[y][x][Cb]);
        b = (int) (yCbCrPicture[y][x][Cb]);

        biCb.setRGB(x, y, (r << 16) | (g << 8) | b);

        // Create Cr image
        r = (int) (yCbCrPicture[y][x][Cr]);
        g = (int) (255 - yCbCrPicture[y][x][Cr]);
        b = 0;

        biCr.setRGB(x, y, (r << 16) | (g << 8) | b);
      }

    ImageIO.write(biY, "png", new File(filePath + "Y.png"));
    ImageIO.write(biCb, "png", new File(filePath + "Cb.png"));
    ImageIO.write(biCr, "png", new File(filePath + "Cr.png"));
  }

}
