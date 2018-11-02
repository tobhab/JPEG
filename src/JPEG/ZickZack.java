package JPEG;

public class ZickZack {

  private int N;
  private int index = 0;
  private int[] result;

  /**
   * DC coding for each depending block (in a row) and build Zig-Zag sequence (block to array).
   * 
   * @param arr
   * @param block_size
   */
  public ZickZack(int[][] arr, int block_size) {

    N = block_size;
    int width = arr[0].length;
    int height = arr.length;

    result = new int[width * height];

    int DC = 0;
    // for each block of image
    for (int y_block = 0; y_block < height; y_block += N) {
      DC = 0;
      for (int x_block = 0; x_block < width; x_block += N)
        DC = zickzack(arr, y_block, x_block, DC);
    }
  }

  public int zickzack(int[][] arr, int y_block, int x_block, int DC) throws Error {
    int DC_temp;

    int rows = y_block;
    int columns = x_block;

    boolean diagonal = true; // true = up, false = down

    DC_temp = arr[rows][columns];
    arr[rows][columns] -= DC;
    result[index++] = arr[rows][columns];
    while (true) {
      if (rows == y_block + N - 1 && columns == x_block + N - 1)
        break;

      if (rows == y_block) { // border up
        if (columns + 1 == x_block + N)
          result[index++] = arr[++rows][columns];
        else
          result[index++] = arr[rows][++columns];
        result[index++] = arr[++rows][--columns];
        diagonal = true;
      } else if (columns == x_block) { // border left
        if (rows + 1 == y_block + N)
          result[index++] = arr[rows][++columns];
        else
          result[index++] = arr[++rows][columns];
        result[index++] = arr[--rows][++columns];
        diagonal = false;
      } else if (rows + 1 == y_block + N) { // border bottom
        result[index++] = arr[rows][++columns];
        if (!(rows == y_block + N - 1 && columns == x_block + N - 1))
          result[index++] = arr[--rows][++columns];
        diagonal = false;
      } else if (columns + 1 == x_block + N) { // border right
        result[index++] = arr[++rows][columns];
        if (rows != y_block + N && columns != x_block + N)
          result[index++] = arr[++rows][--columns];
        diagonal = true;
      } else { // inside of block
        if (diagonal)
          result[index++] = arr[++rows][--columns];
        else
          result[index++] = arr[--rows][++columns];
      }
    }
    return DC_temp;
  }

  public void doIt(int[][] arr, int row, int column) {
    result[index++] = arr[row][column];
  }

  public int[] getResult() {
    return result;
  }

}
