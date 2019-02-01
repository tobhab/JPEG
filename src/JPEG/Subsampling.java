package JPEG;

import static JPEG.Constants.Cb;
import static JPEG.Constants.Cr;
import static JPEG.Constants.Y;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Subsampling {

  private final double[][][] yCbCrPicture;
  double[][] layerY, layerCb, layerCr;

  /**
   * Split the YCbCr array to different layers (Y, Cb, Cr) and subsample the chromatic layers by the subsampling type.
   * Write output images to the file system (local project directory).
   * 
   * @param yCbCrPicture
   * @param subsamplingType
   * @throws IOException
   */
  public Subsampling(double[][][] yCbCrPicture, SubsamplingType subsamplingType) throws IOException {
    this.yCbCrPicture = yCbCrPicture;

    subsample(subsamplingType);

    writeImages("subsample");
  }

  /**
   * Subsample the layers with the arithmetic mean
   * 
   * @param subsamplingType
   */
  private void subsample(SubsamplingType subsamplingType) {
    int height = yCbCrPicture.length;
    int width = yCbCrPicture[0].length;

    layerY = new double[height][width];

    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++)
        layerY[x][y] = yCbCrPicture[x][y][Y];

    switch (subsamplingType) {
    case TYPE_4_1_1:
      layerCb = new double[height][width / 4];
      layerCr = new double[height][width / 4];
      for (int x = 0; x < width; x += 4)
        for (int y = 0; y < height; y++) {
          layerCb[y][x / 4] = arithmeticMean(yCbCrPicture, x, y, Cb, 4, 1);
          layerCr[y][x / 4] = arithmeticMean(yCbCrPicture, x, y, Cr, 4, 1);
        }
      break;
    case TYPE_4_2_0: // default in jpeg
      layerCb = new double[height / 2][width / 2];
      layerCr = new double[height / 2][width / 2];
      for (int x = 0; x < width; x += 2)
        for (int y = 0; y < height; y += 2) {
          layerCb[y / 2][x / 2] = arithmeticMean(yCbCrPicture, x, y, Cb, 2, 2);
          layerCr[y / 2][x / 2] = arithmeticMean(yCbCrPicture, x, y, Cr, 2, 2);
        }
      break;
    case TYPE_4_2_2:
      layerCb = new double[height][width / 2];
      layerCr = new double[height][width / 2];
      for (int x = 0; x < width; x += 2)
        for (int y = 0; y < height; y++) {
          layerCb[y][x / 2] = arithmeticMean(yCbCrPicture, x, y, Cb, 2, 1);
          layerCr[y][x / 2] = arithmeticMean(yCbCrPicture, x, y, Cr, 2, 1);
        }

      break;
    case TYPE_4_4_4:
      layerCb = new double[height][width];
      layerCr = new double[height][width];
      for (int x = 0; x < width; x++)
        for (int y = 0; y < height; y++) {
          layerCb[y][x] = yCbCrPicture[y][x][Cb];
          layerCr[y][x] = yCbCrPicture[y][x][Cr];
        }
    }
  }

  private static double arithmeticMean(double[][][] arr, int x, int y, int color, int offsetX, int offsetY) {
    double mean = 0;

    for (int i = x; i < x + offsetX; i++)
      for (int j = y; j < y + offsetY; j++)
        mean += arr[j][i][color];
    return mean / (offsetX * offsetY);
  }

  public double[][] getLayerY() {
    return layerY;
  }

  public double[][] getLayerCb() {
    return layerCb;
  }

  public double[][] getLayerCr() {
    return layerCr;
  }

  public void writeImages(String filePath) throws IOException {
    if (!JPEG.writeDebugFiles)
      return;

    int height_Y = layerY.length;
    int width_Y = layerY[0].length;

    int height_Cb = layerCb.length;
    int width_Cb = layerCb[0].length;

    int height_Cr = layerCr.length;
    int width_Cr = layerCr[0].length;

    BufferedImage biY = new BufferedImage(width_Y, height_Y, BufferedImage.TYPE_INT_RGB);
    BufferedImage biCb = new BufferedImage(width_Cb, height_Cb, BufferedImage.TYPE_INT_RGB);
    BufferedImage biCr = new BufferedImage(width_Cr, height_Cr, BufferedImage.TYPE_INT_RGB);

    // Create Y image
    for (int x = 0; x < width_Y; x++) 
      for (int y = 0; y < height_Y; y++) {
        int r = (int) (yCbCrPicture[y][x][Y]);
        int g = (int) (yCbCrPicture[y][x][Y]);
        int b = (int) (yCbCrPicture[y][x][Y]);

        biY.setRGB(x, y, (r << 16) | (g << 8) | b);
      }

    // Create Cb image
    for (int x = 0; x < width_Cb; x++) 
      for (int y = 0; y < height_Cb; y++) {
        int r = 0;
        int g = (int) (255 - layerCb[y][x]);
        int b = (int) (layerCb[y][x]);

        biCb.setRGB(x, y, (r << 16) | (g << 8) | b);
      }

    // Create Cr image
    for (int x = 0; x < width_Cr; x++) 
      for (int y = 0; y < height_Cr; y++) {
        int r = (int) (layerCr[y][x]);
        int g = (int) (255 - layerCr[y][x]);
        int b = 0;

        biCr.setRGB(x, y, (r << 16) | (g << 8) | b);
      }

    ImageIO.write(biY, "png", new File(filePath + "Y.png"));
    ImageIO.write(biCb, "png", new File(filePath + "Cb.png"));
    ImageIO.write(biCr, "png", new File(filePath + "Cr.png"));
  }

}
