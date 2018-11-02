package JPEG;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static JPEG.Constants.*;

import javax.imageio.ImageIO;

public class ImageReader {

  private BufferedImage sourceImage;
  
  /**
   * Represent the image in RGB format (int[y][x][RGB])
   */
  private final int[][][] rgbPicture;

  /**
   * Read image from file system and convert it into an array (int[y][x][RGB])
   * 
   * @param imagePath
   * @throws IOException
   */
  public ImageReader(String imagePath) throws IOException {
    sourceImage = readFile(imagePath);
    rgbPicture = convertToRGBArray(sourceImage);
  }

  /**
   * Read file from file system and create a BufferedImage
   * 
   * @param imagePath
   * @return BufferedImage
   * @throws IOException
   */
  private BufferedImage readFile(String imagePath) throws IOException {
    File imgPath = new File(imagePath);
    return ImageIO.read(imgPath);
  }

  /**
   * Convert a BufferedImage to an RGB array
   * 
   * @param BufferedImage
   * @return rgb array (format: int[y][x][RGB])
   */
  private int[][][] convertToRGBArray(BufferedImage bi) {
    int height = bi.getHeight();
    int width = bi.getWidth();
    int[][][] RGBpicture = new int[height][width][3];

    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++) {
        int rgb = bi.getRGB(x, y);

        RGBpicture[y][x][R] = (rgb >> 16) & 0xFF;
        RGBpicture[y][x][G] = (rgb >> 8) & 0xFF;
        RGBpicture[y][x][B] = rgb & 0xFF;
      }
    return RGBpicture;
  }

  public final int[][][] getRGBArray() {
    return rgbPicture;
  }
}
