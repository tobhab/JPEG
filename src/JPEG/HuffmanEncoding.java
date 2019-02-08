package JPEG;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HuffmanEncoding {
  byte[] result;
  boolean showDebugOutput = false;

  /**
   * Encodes the DC and AC componets in the given array with the given huffman trees.
   */
  public HuffmanEncoding(int[] arrY,int[] arrCb,int[] arrCr, int blockCount, HuffmanTree encodingTreeYAC, HuffmanTree encodingTreeYDC, HuffmanTree encodingTreeCxAC, HuffmanTree encodingTreeCxDC) throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BitStreamWriter writer = new BitStreamWriter(out);

    int nextIndexY = 0;
    int nextIndexCb = 0;
    int nextIndexCr = 0;

    int currentBlock = 0;
    //Encode all blocks for the entire array
    while (currentBlock++ < blockCount) {
      if (showDebugOutput) System.out.println("Encoding block " + currentBlock);
      nextIndexY = encodeBlock(arrY, encodingTreeYAC, encodingTreeYDC, writer, nextIndexY, true);
      nextIndexCb = encodeBlock(arrCb, encodingTreeCxAC, encodingTreeCxDC, writer, nextIndexCb, true);
      nextIndexCr = encodeBlock(arrCr, encodingTreeCxAC, encodingTreeCxDC, writer, nextIndexCr, false);

    }
    writer.close();
    out.close();
    result = out.toByteArray();
  }

  private int encodeBlock(int[] arr, HuffmanTree encodingTreeAC, HuffmanTree encodingTreeDC, BitStreamWriter writer, int nextIndex, boolean overrule) throws IOException {

    if(overrule)
    {
      encodingTreeDC.writeCodeToWriter(writer, 0);

      encodingTreeAC.writeCodeToWriter(writer, 0x00);
      return 0;
    }

    if(showDebugOutput) System.out.println("\tWriting the DC component");
    //Encode DC component
    int dcValue = arr[nextIndex++];
    int dcValueBitWidth = getBitWidth(dcValue);

    encodingTreeDC.writeCodeToWriter(writer, dcValueBitWidth);
    if (dcValueBitWidth != 0) //special case of a DC requiring no bits to save the value skips this block of code
    {
      /*
       * Negative dc values are encoded in a special way which is explained in F1.2.1.1 in ITU-T81
       */
      if (dcValue <= 0) {
        dcValue = (int) Math.pow(2, dcValueBitWidth) - 1 + dcValue;
      }

      writer.write(dcValue, dcValueBitWidth);
    }

    if(showDebugOutput) System.out.println("\tWriting the AC components");
    int runlength = 0; //Just need to skip the inital EOB marker
    while(runlength != RunlengthEncode.endOfBlockMarker && runlength != RunlengthEncode.skippingEndOfBlockMarker ) {
      runlength = arr[nextIndex++];
      if (runlength == RunlengthEncode.longZeroRunMarker) {
        //Encode LZR
        encodingTreeAC.writeCodeToWriter(writer, 0xF0);
        continue; //keep going with the next value in this block
      }
      else if(runlength == RunlengthEncode.endOfBlockMarker)
      {
        break;
      }
      else if(runlength == RunlengthEncode.skippingEndOfBlockMarker)
      {
        return nextIndex;
      }

      int acValue = arr[nextIndex++];
      int bitsize = getBitWidth(acValue);

      /*
       * Negative ac values are encoded in a special way which is explained in F1.2.2.1 in ITU-T81
       */
      if (acValue <= 0) {
        acValue = (int) Math.pow(2, bitsize) - 1 + acValue;
      }

      //Encode (runlength,bitsize)(value)
      encodingTreeAC.writeCodeToWriter(writer, (runlength << 4) | bitsize);
      writer.write(acValue, bitsize);

    }

   // nextIndex++; //We need to skip the EOB marker
    //Encode EOB
    encodingTreeAC.writeCodeToWriter(writer, 0x00);
    return nextIndex;
  }

  private int getBitWidth(int value) {
    value = Math.abs(value);
    double returnValue = Math.ceil(log(value + 1, 2));
    return (int) returnValue;
  }

  static double log(int x, int base) {
    return Math.log(x) / Math.log(base);
  }

  public byte[] getResult()
  {
    return result;
  }
}
