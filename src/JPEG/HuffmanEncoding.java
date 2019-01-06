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

      if (dcValueBitWidth == 0) //special case of a DC block being a EOB marker, keep going with the next block
      {
        encodingTreeDC.writeCodeToWriter(writer, dcValueBitWidth);
        continue;
      }

      if (dcValue < 0) {
        dcValue = (int) Math.pow(2, dcValueBitWidth) - 1 + dcValue;
      }

      encodingTreeDC.writeCodeToWriter(writer, dcValueBitWidth);
      writer.write(dcValue, dcValueBitWidth);

      //Encode ACs till an EOB is reached
      while (nextIndex < arr.length && arr[nextIndex] != RunlengthEncode.endOfBlockMarker) {

        int runlength = arr[nextIndex++];
        if (runlength == RunlengthEncode.longZeroRunMarker) {
          //Encode LZR
          encodingTreeAC.writeCodeToWriter(writer, 0xF0);
          break;
        }

        int value = arr[nextIndex++];
        int bitsize = getBitWidth(value);

        //Encode (runlength,bitsize)(value)
        encodingTreeAC.writeCodeToWriter(writer, (runlength << 4) | bitsize);
        writer.write(value, bitsize);
      }
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
