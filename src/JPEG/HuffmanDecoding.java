package JPEG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class HuffmanDecoding {
  int[] result;

  /**
   * Decodes the huffman encoded codes with the given trees
   */
  public HuffmanDecoding(byte[] arr, int blockCount, int blockWidth, HuffmanTree decodingTreeAC, HuffmanTree decodingTreeDC, int[] orig) throws IOException {

    ByteArrayInputStream in = new ByteArrayInputStream(arr);
    BitStreamReader reader = new BitStreamReader(in);

    //create an array which could hold the largest possible resulting data and cut off the unneeded parts later
    result = new int[blockCount * (blockWidth * blockWidth)];
    int indexInResult = 0;

    while (blockCount-- > 0) {
      //read dc value code
      int dcValueBitCount = decodingTreeDC.lookUpCodeNumber(reader);

      if (dcValueBitCount == 0) {
        result[indexInResult++] = 0;
        check(orig, result, indexInResult);
      }
      else {
        //read dc value
        int dcValue = reader.readBits(dcValueBitCount);
        //Reverse the special encoding for negative DC values, see the encoding phase.
        if (dcValue < Math.pow(2, dcValueBitCount - 1)) {
          int twoPowN = (int) Math.pow(2, dcValueBitCount);
          dcValue = dcValue - twoPowN + 1;
        }
        result[indexInResult++] = dcValue;
        check(orig, result, indexInResult);
      }
      //read ac values till a EOB is reached
      do {
        int acValueBitCountRunlengthPair = decodingTreeAC.lookUpCodeNumber(reader);
        if (acValueBitCountRunlengthPair == 0x00) //EOB
        {
          result[indexInResult++] = RunlengthEncode.endOfBlockMarker;
          check(orig, result, indexInResult);
          break;
        } else if (acValueBitCountRunlengthPair == 0xF0) //LZR
        {
          result[indexInResult++] = RunlengthEncode.longZeroRunMarker;
          check(orig, result, indexInResult);
          continue;
        } else {
          int acRunLength = acValueBitCountRunlengthPair >> 4;
          result[indexInResult++] = acRunLength;
          check(orig, result, indexInResult);

          int acBitLength = acValueBitCountRunlengthPair & 0xF;
          int acValue = reader.readBits(acBitLength);

		  //Reverse the special encoding for negative AC values, see the encoding phase.
          if (acValue < Math.pow(2, acBitLength - 1)) {
            int twoPowN = (int) Math.pow(2, acBitLength);
            acValue = acValue - twoPowN + 1;
          }

          result[indexInResult++] = acValue;
          check(orig, result, indexInResult);
        }
      }
      while (true);
    }

    result = Arrays.copyOfRange(result, 0, indexInResult);
    for(int i = 0; i < indexInResult; i++)
    {
     // check(orig,result,i+1);
    }
  }

  private void check(int[] origArray, int[] result, int index)
  {
    index-=1;
    if(index>= origArray.length)
    {
      System.out.println("ERROR: index " + index + " is outside the range for the original array!" );
    }
    else if (origArray[index] != result[index])
    {
      System.out.println("ERROR: index " + index + " should be " + origArray[index] + " but is " + result[index] );
    }
  }

  public int[] getResult() {
    return result;
  }
}
