package JPEG;

import java.util.Arrays;

public class RunlengthEncode {

  private int[] result;

  public final static int endOfBlockMarker = -1;
  public final static int longZeroRunMarker = 17;

  /**
   * Generates a simplified RLE encoding so that no bit operations are needed to read the values in the resulting array.
   * Each AC value is split up into 2 values: Runlength and Value
   * If a run of 16 zeros is reached the longZeroRunMarker is inserted
   * If the end of block is reached without any more non-zero values then the endOfBlockMarker is inserted
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
	//Points to the offset of the block which is currently being processed
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

  /**
   * Performs runlength-encoding for a single block.
   */
  private int encodeBlock(int[] arr, int blockLength, int currentBlockOffset, int currentResultIndex) {
    //Just copy the dc component to the result array, will be compressed in the huffman stage
    result[currentResultIndex++] = arr[currentBlockOffset];
    int indexInCurrentBlock = 0;
	
	//Do runs for the entire block.
    while(indexInCurrentBlock < (blockLength - 1))
    {
      int runLength = 0;

	  //Count the number of zeros which are in the current run and make sure we don't leave the current block
      while(++indexInCurrentBlock <= (blockLength-1) && arr[currentBlockOffset + indexInCurrentBlock] == 0)
      {
        runLength++;
        if(runLength == 16)
        {
	      /*
		   * We reached a situation where there are 16 zeros in the current run.
		   * Since we only have 4 bits to save the length later we need to insert a marker.
		   */
          runLength = 0;
          result[currentResultIndex++] = longZeroRunMarker;
        }
      }

      if(indexInCurrentBlock >= blockLength)
      {
	    /*
		 * Here we have reached the end of the block and didn't encounter any non-zero values for a while.
		 * Since we have inserted long zero runs we need to remove them again...
		 */
        while(result[currentResultIndex - 1] == longZeroRunMarker)
        {
          currentResultIndex -= 1;
        }
		
		/* 
		 * ...and insert an end of block marker here.
		 */
        result[currentResultIndex++] = endOfBlockMarker;
      }
      else
      {
	    /*
		 * We have reached a non-zero value and are still within the block, save the number of zeros and the value after the run.
		 */
        result[currentResultIndex++] = runLength;
        result[currentResultIndex++] = arr[currentBlockOffset + indexInCurrentBlock ];
      }
    }
	
    //According to ITU-t81 F.1.2.3 the last EOB marker is bypassed if the very last coefficient is already set

    return currentResultIndex;
  }

  public int[] getResult() {
    return result;
  }
}
