package JPEG;

public class Constants {

  // Internal constants
  final static int R = 0;
  final static int G = 1;
  final static int B = 2;

  final static int Y = 0;
  final static int Cb = 1;
  final static int Cr = 2;

  /*
   * Different predefined matrices for color conversion
   */
  public final static double[][] YPbPrMatrix = {
      { 0.299, 0.587, 0.114 },
      { -0.168736, -0.331264, 0.5 },
      { 0.5, -0.418688, -0.081312 }
  };

  public final static double[][] YPbPrMatrix2 = {
      { 65.738, 129.057, 25.064 },
      { -37.945, -74.494, 112.439 },
      { 112.439, -94.154, -18.285 }
  };

  public final static double[][] RGBMatrix1 = {
      { 1.0, 0, 1.402 },
      { 1.0, -0.344136, -0.714136 },
      { 1.0, 1.772, 0 }
  };

  public final static double[][] YCbCrMatrix = {
      { 0.0 },
      { 128.0 },
      { 128.0 }
  };

  /*
   * Different predefined quantization matrices for different quality levels
   */
  public final static int[][] Q20 = {
      { 3, 5, 7, 9, 11, 13, 15, 17 },
      { 5, 7, 9, 11, 13, 15, 17, 19 },
      { 7, 9, 11, 13, 15, 17, 19, 21 },
      { 9, 11, 13, 15, 17, 19, 21, 23 },
      { 11, 13, 15, 17, 19, 21, 23, 25 },
      { 13, 15, 17, 19, 21, 23, 25, 27 },
      { 15, 17, 19, 21, 23, 25, 27, 29 },
      { 17, 19, 21, 23, 25, 27, 29, 31 }
  };

  public final static int[][] Q50 = {
      { 16, 11, 10, 16, 24, 40, 51, 61 },
      { 12, 12, 14, 19, 26, 58, 60, 55 },
      { 14, 13, 16, 24, 40, 57, 69, 56 },
      { 14, 17, 22, 29, 51, 87, 80, 62 },
      { 18, 22, 37, 56, 68, 109, 103, 77 },
      { 24, 35, 55, 64, 81, 104, 113, 92 },
      { 49, 64, 78, 87, 103, 121, 120, 101 },
      { 72, 92, 95, 98, 112, 100, 103, 99 }
  };

  public final static int[][] Q70 = {
      { 10, 15, 25, 37, 51, 66, 82, 100 },
      { 15, 19, 28, 39, 52, 67, 83, 101 },
      { 25, 28, 35, 45, 58, 72, 88, 105 },
      { 37, 39, 45, 54, 66, 79, 94, 111 },
      { 51, 52, 58, 66, 76, 89, 103, 119 },
      { 66, 67, 72, 79, 89, 101, 114, 130 },
      { 82, 83, 88, 94, 103, 114, 127, 142 },
      { 100, 101, 105, 111, 119, 130, 142, 156 }
  };

  public final static int[][] Q100 = {
      { 17, 18, 24, 47, 99, 99, 99, 99 },
      { 18, 21, 26, 66, 99, 99, 99, 99 },
      { 24, 26, 56, 99, 99, 99, 99, 99 },
      { 47, 66, 99, 99, 99, 99, 99, 99 },
      { 99, 99, 99, 99, 99, 99, 99, 99 },
      { 99, 99, 99, 99, 99, 99, 99, 99 },
      { 99, 99, 99, 99, 99, 99, 99, 99 },
      { 99, 99, 99, 99, 99, 99, 99, 99 }
  };

  public final static int[][] Q255 = {
      { 80, 60, 50, 80, 120, 200, 255, 255 },
      { 55, 60, 70, 95, 130, 255, 255, 255 },
      { 70, 65, 80, 120, 200, 255, 255, 255 },
      { 70, 85, 110, 145, 255, 255, 255, 255 },
      { 90, 110, 185, 255, 255, 255, 255, 255 },
      { 120, 175, 255, 255, 255, 255, 255, 255 },
      { 245, 255, 255, 255, 255, 255, 255, 255 },
      { 255, 255, 255, 255, 255, 255, 255, 255 }
  };

}
