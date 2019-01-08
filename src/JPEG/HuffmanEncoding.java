package JPEG;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HuffmanEncoding {
  byte[] result;

  public HuffmanEncoding(int[] arr, HuffmanTree encodingTreeAC, HuffmanTree encodingTreeDC) throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BitStreamWriter writer = new BitStreamWriter(out);

    int nextIndex = 0;

    //Encode all blocks for the entire array
    while (nextIndex < arr.length) {
      //Encode DC component
      int dcValue = arr[nextIndex++];
      int dcValueBitWidth = getBitWidth(dcValue);

      encodingTreeDC.writeCodeToWriter(writer, dcValueBitWidth);
      if (dcValueBitWidth == 0) //special case of a DC block being a EOB marker, keep going with the next block
      {
        continue;
      }

      if (dcValue <= 0) {
        dcValue = (int) Math.pow(2, dcValueBitWidth) - 1 + dcValue;
      }

      writer.write(dcValue, dcValueBitWidth);

      //Encode ACs till an EOB is reached
      while (nextIndex < arr.length && arr[nextIndex] != RunlengthEncode.endOfBlockMarker) {

        int runlength = arr[nextIndex++];
        if (runlength == RunlengthEncode.longZeroRunMarker) {
          //Encode LZR
          encodingTreeAC.writeCodeToWriter(writer, 0xF0);
          continue; //keep going with the next value in this block
        }

        int acValue = arr[nextIndex++];
        int bitsize = getBitWidth(acValue);

        if(acValue <= 0)
        {
          acValue = (int) Math.pow(2, bitsize) - 1 + acValue;
        }

        //Encode (runlength,bitsize)(value)
        encodingTreeAC.writeCodeToWriter(writer, (runlength << 4) | bitsize);
        writer.write(acValue, bitsize);
      }
      nextIndex++; //We need to skip the EOB marker
      //Encode EOB
      encodingTreeAC.writeCodeToWriter(writer, 0x00);
    }
    writer.close();
    result = out.toByteArray();
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
