package JPEG;

public class ZickZack {

  private int N;
  private int[] result;

  //The value in this field indicates at which index-1 in the 1d final array a cell ends up
  private final int[][] zickZackMapping8 = {
          {1, 2, 6, 7, 15, 16, 28, 29},
          {3, 5, 8, 14, 17, 27, 30, 43},
          {4, 9, 13, 18, 26, 31, 42, 44},
          {10, 12, 19, 25, 32, 41, 45, 54},
          {11, 20, 24, 33, 40, 46, 53, 55},
          {21, 23, 34, 39, 47, 52, 56, 61},
          {22, 35, 38, 48, 51, 57, 60, 62},
          {36, 37, 49, 50, 58, 59, 63, 64}};

  /**
   * Build Zig-Zag sequence (block to array).
   *
   * @param arr
   * @param block_size
   */
  public ZickZack(int[][] arr, int block_size) throws Exception {
    if (block_size != 8) {
      throw new Exception("Zickzack is only supported on a blocksize of 8!");
    }

    N = block_size;
    int width = arr[0].length;
    int height = arr.length;

    result = new int[width * height];

    int offsetForBlock = 0;
    // for each block of image
    for (int y_block = 0; y_block < height; y_block += N)
    {
      for (int x_block = 0; x_block < width; x_block += N)
      {
        zickzack(arr, y_block, x_block, offsetForBlock);
        offsetForBlock += N*N;
      }
    }
  }

  public void zickzack(int[][] arr, int y_block, int x_block, int offsetForBlock) throws Error {
    int rows = y_block;
    int columns = x_block;

    for (int x = 0; x < N; x++) {
      for (int y = 0; y < N; y++) {
        int indexInBlock = zickZackMapping8[x][y] - 1;
        result[offsetForBlock + indexInBlock] = arr[x_block + x][y_block + y];
      }
    }
  }

  public int[] getResult() {
    return result;
  }

}
