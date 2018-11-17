package JPEG;

import static JPEG.Constants.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ReverseSubsampling {

  private double[][][] yCbCrPicture;
  double[][] layerY, layerCb, layerCr;

  /**
   * Revert the subsampling of all layers (Y, Cb, Cr) depending on the subsamplingType and write to one YCbCr array. 
   * Write reverted subsample images to the file system (local project directory).
   * 
   * @param layerY
   * @param layerCb
   * @param layerCr
   * @param subsamplingType
   * @throws IOException
   */
  public ReverseSubsampling(double[][] layerY, double[][] layerCb, double[][] layerCr, SubsamplingType subsamplingType) throws IOException {
    int height = layerY.length;
    int width = layerY[0].length;

    yCbCrPicture = new double[height][width][3];
    this.layerY = layerY;
    this.layerCb = layerCb;
    this.layerCr = layerCr;

    subsample(subsamplingType);

    writeImages("ReverseSubsample");
  }

  private void subsample(SubsamplingType subsamplingType) {
    int height = layerCb.length;
    int width = layerCb[0].length;

    for (int x = 0; x < yCbCrPicture[0].length; x++)
      for (int y = 0; y < yCbCrPicture.length; y++)
        yCbCrPicture[x][y][Y] = layerY[x][y];

    switch (subsamplingType) {
    case TYPE_4_1_1:
      for (int x = 0; x < width; x++)
        for (int y = 0; y < height; y++)
          for (int i = 0; i < 4; i++) {
            yCbCrPicture[y][4 * x + i][Cb] = layerCb[y][x];
            yCbCrPicture[y][4 * x + i][Cr] = layerCr[y][x];
          }
      break;
    case TYPE_4_2_0: // default in jpeg
      for (int x = 0; x < width; x++)
        for (int y = 0; y < height; y++) {
          for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++) {
              yCbCrPicture[2 * y + i][2 * x + j][Cb] = layerCb[y][x];
              yCbCrPicture[2 * y + i][2 * x + j][Cr] = layerCr[y][x];
            }
        }
      break;
    case TYPE_4_2_2:
      for (int x = 0; x < width; x++)
        for (int y = 0; y < height; y++)
          for (int i = 0; i < 2; i++) {
            yCbCrPicture[y][2 * x + i][Cb] = layerCb[y][x];
            yCbCrPicture[y][2 * x + i][Cr] = layerCr[y][x];
          }
      break;
    case TYPE_4_4_4:
      for (int x = 0; x < width; x++)
        for (int y = 0; y < height; y++) {
          yCbCrPicture[y][x][Cb] = layerCb[y][x];
          yCbCrPicture[y][x][Cr] = layerCr[y][x];
        }
      break;
    }

  }

  public double[][][] getYCbCr() {
    return yCbCrPicture;
  }

  public void writeImages(String filePath) throws IOException {
    if(!JPEG.writeDebugFiles)
      return;

    int height = yCbCrPicture.length;
    int width = yCbCrPicture[0].length;
    
//    int height_Y = layerY.length;
//    int width_Y = layerY[0].length;
//
//    int height_Cb = layerCb.length;
//    int width_Cb = layerCb[0].length;
//
//    int height_Cr = layerCr.length;
//    int width_Cr = layerCr[0].length;

  BufferedImage biY = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  BufferedImage biCb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  BufferedImage biCr = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
//    BufferedImage biY = new BufferedImage(width_Y, height_Y, BufferedImage.TYPE_INT_RGB);
//    BufferedImage biCb = new BufferedImage(width_Cb, height_Cb, BufferedImage.TYPE_INT_RGB);
//    BufferedImage biCr = new BufferedImage(width_Cr, height_Cr, BufferedImage.TYPE_INT_RGB);

//    // Create Y image
//    for (int x = 0; x < width_Y; x++)
//      for (int y = 0; y < height_Y; y++) {
//        int r = (int) (yCbCrPicture[y][x][Y]);
//        int g = (int) (yCbCrPicture[y][x][Y]);
//        int b = (int) (yCbCrPicture[y][x][Y]);
//
//        biY.setRGB(x, y, (r << 16) | (g << 8) | b);
//      }
    
    // Create Y image
    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++) {
        int r = (int) (yCbCrPicture[y][x][Y]);
        int g = (int) (yCbCrPicture[y][x][Y]);
        int b = (int) (yCbCrPicture[y][x][Y]);

        biY.setRGB(x, y, (r << 16) | (g << 8) | b);
      }
    
    
    // Create Cb image
    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++) {
        
        int r = 0;
        int g = (int) (255 - yCbCrPicture[y][x][Cb]);
        int b = (int) (yCbCrPicture[y][x][Cb]);

        biCb.setRGB(x, y, (r << 16) | (g << 8) | b);
      }

//    // Create Cb image
//    for (int x = 0; x < width_Cb; x++)
//      for (int y = 0; y < height_Cb; y++) {
//        int r = 0;
//        int g = (int) (255 - layerCb[y][x]);
//        int b = (int) (layerCb[y][x]);
//
//        biCb.setRGB(x, y, (r << 16) | (g << 8) | b);
//      }
    
    
    // Create Cr image
    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++) {
        
        int r = (int) (yCbCrPicture[y][x][Cr]);
        int g = (int) (255- yCbCrPicture[y][x][Cr]);
        int b = 0;

        biCr.setRGB(x, y, (r << 16) | (g << 8) | b);
      }
    
//    // Create Cr image
//    for (int x = 0; x < width_Cr; x++)
//      for (int y = 0; y < height_Cr; y++) {
//        int r = (int) (layerCr[y][x]);
//        int g = (int) (255 - layerCr[y][x]);
//        int b = 0;
//
//        biCr.setRGB(x, y, (r << 16) | (g << 8) | b);
//      }

    ImageIO.write(biY, "png", new File(filePath + "Y.png"));
    ImageIO.write(biCb, "png", new File(filePath + "Cb.png"));
    ImageIO.write(biCr, "png", new File(filePath + "Cr.png"));
  }

}
