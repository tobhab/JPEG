package JPEG;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class HuffmanDecoding {
  int[] resultY;
  int[] resultCb;
  int[] resultCr;

  /**
   * Decodes the huffman encoded codes with the given trees
   */
  public HuffmanDecoding(byte[] arr, int blockCount, int blockWidth, HuffmanTree decodingTreeYAC, HuffmanTree decodingTreeYDC,HuffmanTree decodingTreeCxAC, HuffmanTree decodingTreeCxDC) throws IOException {

    ByteArrayInputStream in = new ByteArrayInputStream(arr);
    BitStreamReader reader = new BitStreamReader(in);

    //create an array which could hold the largest possible resulting data and cut off the unneeded parts later
    resultY = new int[blockCount * (blockWidth * blockWidth)];
    resultCb = new int[blockCount * (blockWidth * blockWidth)];
    resultCr = new int[blockCount * (blockWidth * blockWidth)];
    int indexInResultY = 0;
    int indexInResultCb = 0;
    int indexInResultCr = 0;

    while (blockCount-- > 0) {
      indexInResultY = decodeBlock(decodingTreeYAC, decodingTreeYDC, reader, indexInResultY,resultY);
      indexInResultCb = decodeBlock(decodingTreeCxAC, decodingTreeCxDC, reader, indexInResultCb,resultCb);
      indexInResultCr = decodeBlock(decodingTreeCxAC, decodingTreeCxDC, reader, indexInResultCr,resultCr);

    }

    resultY = Arrays.copyOfRange(resultY, 0, indexInResultY);
    resultCb = Arrays.copyOfRange(resultCb, 0, indexInResultCb);
    resultCr = Arrays.copyOfRange(resultCr, 0, indexInResultCr);

  }

  private int decodeBlock(HuffmanTree decodingTreeYAC, HuffmanTree decodingTreeYDC, BitStreamReader reader, int indexInResult, int[]outputArray) throws IOException {
    //read dc value code
    int dcValueBitCount = decodingTreeYDC.lookUpCodeNumber(reader);

    if (dcValueBitCount == 0) {
      outputArray[indexInResult++] = 0;
    }
    else {
      //read dc value
      int dcValue = reader.readBits(dcValueBitCount);
      //Reverse the special encoding for negative DC values, see the encoding phase.
      if (dcValue < Math.pow(2, dcValueBitCount - 1)) {
        int twoPowN = (int) Math.pow(2, dcValueBitCount);
        dcValue = dcValue - twoPowN + 1;
      }
      outputArray[indexInResult++] = dcValue;
    }
    //read ac values till a EOB is reached
    do {
      int acValueBitCountRunlengthPair = decodingTreeYAC.lookUpCodeNumber(reader);
      if (acValueBitCountRunlengthPair == 0x00) //EOB
      {
        outputArray[indexInResult++] = RunlengthEncode.endOfBlockMarker;
        break;
      } else if (acValueBitCountRunlengthPair == 0xF0) //LZR
      {
        outputArray[indexInResult++] = RunlengthEncode.longZeroRunMarker;
        continue;
      } else {
        int acRunLength = acValueBitCountRunlengthPair >> 4;
        outputArray[indexInResult++] = acRunLength;

        int acBitLength = acValueBitCountRunlengthPair & 0xF;
        int acValue = reader.readBits(acBitLength);

        //Reverse the special encoding for negative AC values, see the encoding phase.
        if (acValue < Math.pow(2, acBitLength - 1)) {
          int twoPowN = (int) Math.pow(2, acBitLength);
          acValue = acValue - twoPowN + 1;
        }

        outputArray[indexInResult++] = acValue;
      }
    }
    while (true);
    return indexInResult;
  }

  public int[] getResultY() {
    return resultY;
  }
  public int[] getResultCb() {
    return resultCb;
  }
  public int[] getResultCr() {
    return resultCr;
  }
}
