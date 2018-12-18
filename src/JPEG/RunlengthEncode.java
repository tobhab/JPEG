package JPEG;

import java.util.Arrays;

public class RunlengthEncode {

  private int[] result;

  public final int endOfBlockMarker = -1;
  public final int longZeroRun = 17;

  /**
   * Generates a simplified RLE encoding so that no bit operations are needed to read the values in the resulting array.
   * Each AC value is split up into 2 values: Runlength and Value
   * The pair (17,X) means a run of more then 16 zeros before a value.
   * The pair (-1,X) means end of block
   *
   * @param arr
   * @param block_size
   */
  public RunlengthEncode(int[] arr, int block_size)
  {
    //blockLength is the length of an 2d square with the side length of block_size
    int blockLength = block_size * block_size;
    //Points to the next free index where data can be written to
    int currentResultIndex = 0;
    int currentBlockOffset = 0;
    /*
     * This is the longest a RLE could be, we just made sure that this always fits into the destination array.
     * We only return a part of that array after we are done here.
     */
    result = new int[arr.length * 2];

    do
    {
      currentResultIndex = encodeBlock(arr, blockLength, currentBlockOffset, currentResultIndex);


      currentBlockOffset += blockLength;
    }
    while(currentBlockOffset < arr.length);

    result = Arrays.copyOfRange(result,0,currentResultIndex);
  }

  private int encodeBlock(int[] arr, int blockLength, int currentBlockOffset, int currentResultIndex) {
    //Just copy the dc component to the result array, will be compressed in the huffman stage
    result[currentResultIndex++] = arr[currentBlockOffset];
    int indexInCurrentBlock = 0;
    while(indexInCurrentBlock < (blockLength - 1))
    {
      int runLength = 0;

      while(++indexInCurrentBlock <= (blockLength-1) && arr[currentBlockOffset + indexInCurrentBlock] == 0)
      {
        runLength++;
        if(runLength == 16)
        {
          //System.out.println("long run");
          runLength = 0;
          result[currentResultIndex++] = longZeroRun;
          result[currentResultIndex++] = 0;
        }
      }

      if(indexInCurrentBlock >= blockLength)
      {
        while(result[currentResultIndex - 2] == longZeroRun)
        {
          //System.out.println("rewind long run");
          currentResultIndex -= 2;
        }
        //System.out.println("EOB @" + (currentBlockOffset + indexInCurrentBlock));
        result[currentResultIndex++] = endOfBlockMarker;
        result[currentResultIndex++] = 0;
      }
      else
      {
        //System.out.println("(" + runLength + "," + arr[currentBlockOffset + indexInCurrentBlock ] + ")");
        result[currentResultIndex++] = runLength;
        result[currentResultIndex++] = arr[currentBlockOffset + indexInCurrentBlock ];
      }
    }

    return currentResultIndex;
  }

  public int[] getResult() {
    return result;
  }

}
