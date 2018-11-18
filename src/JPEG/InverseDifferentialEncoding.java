package JPEG;

public class InverseDifferentialEncoding {

  private int[][] result;

  /**
   * Inverse DC coding for each depending block (in a row)
   *
   * @param arr
   * @param block_size
   */
  public InverseDifferentialEncoding(int[][] arr, int block_size) throws Exception {

    int width = arr[0].length;
    int height = arr.length;

    result = DifferentialEncoding.deepCopy(arr);

    int PrevioudD = 0;

    for (int y_block = 0; y_block < height; y_block += block_size)
    {
      PrevioudD = 0;
      for (int x_block = 0; x_block < width; x_block += block_size)
      {
        result[y_block][x_block] += PrevioudD;
        PrevioudD = result[y_block][x_block];
      }
    }
  }

  public int[][] getResult() {
    return result;
  }

}
