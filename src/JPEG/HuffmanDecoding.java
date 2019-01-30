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
  public HuffmanDecoding(byte[] arr, int blockCount, int blockWidth, HuffmanTree decodingTreeAC, HuffmanTree decodingTreeDC) throws IOException {

    ByteArrayInputStream in = new ByteArrayInputStream(arr);
    BitStreamReader reader = new BitStreamReader(in);

    //create an array which could hold the largest possible resulting data and cut off the unneeded parts later
    result = new int[blockCount * (blockWidth * blockWidth)];
    int indexInResult = 0;

    while (blockCount-- > 0) {
      //read dc value code
      int dcValueCode = decodingTreeDC.lookUpCodeNumber(reader);

      //skip to next block if it's a EOB marker
      if (dcValueCode == 0) {
        result[indexInResult++] = 0;
        continue;
      }
      //read dc value
      int dcValue = reader.readBits(dcValueCode);
	  //Reverse the special encoding for negative DC values, see the encoding phase.
      if (dcValue < Math.pow(2, dcValueCode - 1)) {
        int twoPowN = (int) Math.pow(2, dcValueCode);
        dcValue = dcValue - twoPowN + 1;
      }
      result[indexInResult++] = dcValue;

      //read ac values till a EOB is reached
      do {
        int acValueCode = decodingTreeAC.lookUpCodeNumber(reader);
        if (acValueCode == 0x00) //EOB
        {
          result[indexInResult++] = RunlengthEncode.endOfBlockMarker;
          break;
        } else if (acValueCode == 0xF0) //LZR
        {
          result[indexInResult++] = RunlengthEncode.longZeroRunMarker;
          continue;
        } else {
          int acRunLength = acValueCode >> 4;
          result[indexInResult++] = acRunLength;

          int acBitLength = acValueCode & 0xF;
          int acValue = reader.readBits(acBitLength);

		  //Reverse the special encoding for negative AC values, see the encoding phase.
          if (acValue < Math.pow(2, acBitLength - 1)) {
            int twoPowN = (int) Math.pow(2, acBitLength);
            acValue = acValue - twoPowN + 1;
          }

          result[indexInResult++] = acValue;
        }
      }
      while (true);
    }

    result = Arrays.copyOfRange(result, 0, indexInResult);
  }

  public int[] getResult() {
    return result;
  }
}
