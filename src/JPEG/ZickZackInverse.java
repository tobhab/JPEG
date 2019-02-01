package JPEG;

public class ZickZackInverse {

  private int N;
  private int[][] result;

  //The inner array represents at which location in the 2d array the location in the 1d array ends up
  private int[][] ReverseZickZackMapping8 = {
          {0, 0}, {0, 1}, {1, 0}, {2, 0}, {1, 1}, {0, 2}, {0, 3}, {1, 2},
          {2, 1}, {3, 0}, {4, 0}, {3, 1}, {2, 2}, {1, 3}, {0, 4}, {0, 5},
          {1, 4}, {2, 3}, {3, 2}, {4, 1}, {5, 0}, {6, 0}, {5, 1}, {4, 2},
          {3, 3}, {2, 4}, {1, 5}, {0, 6}, {0, 7}, {1, 6}, {2, 5}, {3, 4},
          {4, 3}, {5, 2}, {6, 1}, {7, 0}, {7, 1}, {6, 2}, {5, 3}, {4, 4},
          {3, 5}, {2, 6}, {1, 7}, {2, 7}, {3, 6}, {4, 5}, {5, 4}, {6, 3},
          {7, 2}, {7, 3}, {6, 4}, {5, 5}, {4, 6}, {3, 7}, {4, 7}, {5, 6},
          {6, 5}, {7, 4}, {7, 5}, {6, 6}, {5, 7}, {6, 7}, {7, 6}, {7, 7}};

  /**
   * Inverse the zickzack-encoding (array to block).
   *
   * @param arr
   * @param block_size
   */
  public ZickZackInverse(int[] arr, int block_size, int width, int height) throws Exception {
    if (block_size != 8) {
      throw new Exception("Inverse Zickzack is only supported on a blocksize of 8!");
    }
    N = block_size;

    result = new int[width][height];

    int offset = 0;
    // For each row in the image...
    for (int y_block = 0; y_block < height; y_block += N)
    {
      // ... for each row in the image...
      for (int x_block = 0; x_block < width; x_block += N)
      {
        //... we are inversing the zickzack encoding
        zickzackInverseBlock(arr, offset, y_block, x_block);
        offset += N * N;
      }
    }
  }

  private void zickzackInverseBlock(int[] arr, int offsetInArr, int y_block, int x_block) throws Error {
    for (int i = 0; i < (N * N); i++) {
      int y_offset = ReverseZickZackMapping8[i][0];
      int x_offset = ReverseZickZackMapping8[i][1];
      result[y_block + y_offset][x_block + x_offset] = arr[offsetInArr + i];
    }
  }

  public int[][] getResult() {
    return result;
  }

}
