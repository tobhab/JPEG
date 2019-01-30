package JPEG;

public class RunlengthDecode {

  private int[] result;

  /**
   * Reverses the simplified RLE encoding so that no bit operations are needed to decode values in the from the RLE array.
   *
   * @param arr
   * @param block_size
   * @param blockCount
   */
  public RunlengthDecode(int[] arr, int block_size, int blockCount) {
    //blockLength is the length of an 2d square with the side length of block_size
    int blockLength = block_size * block_size;
    //Points to the next free index where data can be written to
    int currentWriteIndex = 0;
    //Points to the index which will be decoded next
    int currentDecodeIndex = 0;

    result = new int[blockLength * blockCount];

    while (blockCount-- > 0) {
      //Copy DC value into the output array
      result[currentWriteIndex++] = arr[currentDecodeIndex++];
      while (true) {
        //Check if we reach an end of block
        if (arr[currentDecodeIndex] == RunlengthEncode.endOfBlockMarker) {
          currentDecodeIndex++;
          //skip to the next block boundary
          currentWriteIndex = ((currentWriteIndex + blockLength - 1) / blockLength) * blockLength;
          break;
		//Check if we reach a long zero run marker
        } else if (arr[currentDecodeIndex] == RunlengthEncode.longZeroRunMarker) {
          currentDecodeIndex++;
          //skip over the zeros
          currentWriteIndex += 16;
        }
        else
        {
          //skip over the zeros...
          currentWriteIndex += arr[currentDecodeIndex++];
          //...and add the value behind that
          result[currentWriteIndex++] = arr[currentDecodeIndex++];
        }
      }
    }
  }

  public int[] getResult() {
    return result;
  }

}
