package JPEG;

public class DifferentialEncoding {

  private int[][] result;

  /**
   * DC coding for each depending block (in a row)
   *
   * @param arr
   * @param block_size
   */
  public DifferentialEncoding(int[][] arr, int block_size) throws Exception
  {
    int width = arr[0].length;
    int height = arr.length;

    result = deepCopy(arr);

    int D = 0;

    for (int y_block = 0; y_block < height; y_block += block_size)
    {
      D = 0;
      for (int x_block = 0; x_block < width; x_block += block_size)
      {
        int D_temp = result[y_block][x_block];
        result[y_block][x_block] -= D;
        D = D_temp;
      }
    }
  }

  public static int[][] deepCopy(int[][] original) {
    if (original == null) {
      return null;
    }

    final int[][] result = new int[original.length][];
    for (int i = 0; i < original.length; i++) {
      result[i] = java.util.Arrays.copyOf(original[i], original[i].length);
    }
    return result;
  }

  public int[][] getResult() {
    return result;
  }

}
