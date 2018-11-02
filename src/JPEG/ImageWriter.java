package JPEG;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import static JPEG.Constants.*;

public class ImageWriter {

  public ImageWriter(int[][][] rgbArray, String imagePath) throws IOException {
    BufferedImage bi = convertToBufferedImage(rgbArray);
    writeFile(bi, imagePath);
  }

  /**
   * Write a BufferedImage to the file system 
   * 
   * @param bi
   * @param imagePath
   * @throws IOException
   */
  public void writeFile(BufferedImage bi, String imagePath) throws IOException {
    RenderedImage rendImage = bi;
    ImageIO.write(rendImage, "bmp", new File(imagePath));
  }

  /**
   * Convert a RGB array to a BufferedImage
   * 
   * @param rgb
   * @return BufferedImage
   */
  public BufferedImage convertToBufferedImage(int[][][] rgb) {
    int width = rgb[0].length;
    int height = rgb.length;

    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++)
      for (int x = 0; x < width; x++) {
        int rgbInt = rgb[y][x][R];
        rgbInt = (rgbInt << 8) + rgb[y][x][G];
        rgbInt = (rgbInt << 8) + rgb[y][x][B];
        bi.setRGB(x, y, rgbInt);
      }

    return bi;
  }
}
