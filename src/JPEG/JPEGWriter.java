package JPEG;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class JPEGWriter {

  public JPEGWriter(int width, int height, String fileName,
                    HuffmanTree huffmanChromAC, HuffmanTree huffmanChromDC, HuffmanTree huffmanLumAC, HuffmanTree huffmanLumDC,
                    int[][] quantLum, int[][] quantChrom,
                    SubsamplingType subsamplingType,
                    byte[] scanData_Cb, byte[] scanData_Cr, byte[] scanData_Y) throws Exception {
    try {
      FileOutputStream stream = new FileOutputStream(fileName);
      stream.write(getSOI());
      stream.write(getAPP0());
      stream.write(getQuantizationMatrixHeader( quantLum, true));
      stream.write(getQuantizationMatrixHeader(quantChrom, false));
      stream.write(getSOF(width, height, subsamplingType));
      stream.write(getHuffmanTableHeader(huffmanChromAC, false, false));
      stream.write(getHuffmanTableHeader(huffmanChromDC, false, true));
      stream.write(getHuffmanTableHeader(huffmanLumAC, true, false));
      stream.write(getHuffmanTableHeader(huffmanLumDC, true, true));
      stream.write(getSOS());
      stream.write(getScanData(scanData_Cb,scanData_Cr,scanData_Y));
      stream.write(getEOI());
      stream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private byte[] getSOI()
  {
    return new byte[]{(byte)0xFF, (byte)0xD8};
  }

  private byte[] getAPP0() {
    return new byte[]{(byte) 0xFF, (byte) 0xE0,                               //Header id
            (byte) 0x00, (byte) 0x10,                                         //length of the APP0 Header
            (byte) 0x4A, (byte) 0x46, (byte) 0x49, (byte) 0x46, (byte) 0x00,  //identifier (String "JFIF")
            (byte) 0x01, (byte) 0x01,                                         //version of JPEG format
            (byte) 0x00,                                                      //units
            (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01,               //X, Y density
            (byte) 0x00, (byte) 0x00                                          //X, Y thumbnail size, (0,0) means no thumbnail
    };
  }

  private byte[] getQuantizationMatrixHeader(int[][] quantizationMatrix, boolean isLuminance) throws Exception {
    //First get the information which is the same for each quant matrix
    byte[] baseInformation  = new byte[]{(byte) 0xFF, (byte) 0xDB,  //Header id
            (byte) 0x00, (byte) 0x43,                               //length of the DQT Header
            (byte)( (0x00 << 4) |                                   //Accuracy of the values encoded with 4 bits, 0 says 8bit per value and 1 says 16bits per value
                    (isLuminance ? ( 0x00):0x01)),                  //Table number encoded in 4 bits, 0 for luminance and 1 for the color
    };

    //Then we convert the 2d quant matrix into the 1d byte array, which must use the zickzack encoding
    ZickZack quantTableHelper = new ZickZack(quantizationMatrix, 8);
    byte[] quantTable = quantTableHelper.getByteResult();

    //Then we combine the 2 arrays and are finished here
    byte[] returnArray= new byte[baseInformation.length + quantTable.length];
    System.arraycopy(baseInformation, 0, returnArray, 0, baseInformation.length);
    System.arraycopy(quantTable, 0, returnArray, baseInformation.length, quantTable.length);
    return returnArray;
  }

  private byte[] getHuffmanTableHeader(HuffmanTree tree, boolean isLuminance, boolean isDC) {
    byte[][] treeArrays = tree.getArrays();

    int headerlength = 3 + treeArrays[0].length + treeArrays[1].length;

    //First get the information which is the same for each huffman tree
    byte[] baseInformation = new byte[]{(byte) 0xFF, (byte) 0xC4,  //Header id
            (byte) (headerlength>>8), (byte) headerlength,                               //length of the DHT Header
            (byte) ((isDC ? 0x0 : 0x1) << 4 |                      //class
                    (isLuminance ? 0x0 : 0x1))                        //number
    };

    byte[] returnArray = new byte[baseInformation.length + treeArrays[0].length + treeArrays[1].length];
    System.arraycopy(baseInformation, 0, returnArray, 0, baseInformation.length);
    System.arraycopy(treeArrays[0], 0, returnArray, baseInformation.length, treeArrays[0].length);
    System.arraycopy(treeArrays[1], 0, returnArray, baseInformation.length + treeArrays[0].length, treeArrays[1].length);
    return  returnArray;
  }

  private byte[] getSOS() //See itu-t81.pdf B.2.3
  {
    return new byte[]{(byte)0xFF, (byte)0xDA,//Header id
            (byte)0x00,(byte)0x0C,  //length of block
            (byte)0x03, //3 components, Y, Cr and Cb
            (byte)0x01, //Component 1 ID
            (byte)0x00, //Component 1 DC and AC selector
            (byte)0x02, //Component 2 ID
            (byte)0x11, //Component 2 DC and AC selector
            (byte)0x03, //Component 3 ID
            (byte)0x11, //Component 3 DC and AC selector
            (byte)0x00, //spectral selection start ???
            (byte)0x3F, //spectral selection end
            (byte)0x00  //bitlevel high and low each 0
    };
  }

  private byte[] getSOF(int imageWidth, int imageHeight, SubsamplingType subsamplingType)
  {
    byte subsamplingHeight = 0;
    byte subsamplingWidtht = 0;
    switch (  subsamplingType   ){
      case TYPE_4_2_2: //full height and half width, according to given subsampling in class Subsampling
        subsamplingHeight = 4;
        subsamplingWidtht = 2;
        break;
      case TYPE_4_1_1://full height and quater width, according to given subsampling in class Subsampling
        subsamplingHeight = 4;
        subsamplingWidtht = 1;
        break;
      case TYPE_4_2_0://half height and half width, according to given subsampling in class Subsampling
        subsamplingHeight = 2;
        subsamplingWidtht = 2;
        break;
      case TYPE_4_4_4://full resolution, according to given subsampling in class Subsampling
        subsamplingHeight = 4;
        subsamplingWidtht = 4;
        break;
    }
    subsamplingHeight = 1;
    subsamplingWidtht = 1;

    return new byte[]{(byte)0xFF, (byte)0xC1,//Header id
            (byte)0x00,(byte)0x11,  //length of block
            (byte)0x08, //bits pro pixel pro component
            (byte)(imageHeight >> 8), (byte)(imageHeight),
            (byte)(imageWidth >> 8), (byte)(imageWidth),
            (byte)0x03, //Number of components, 3 for Y, Cb, Cr
            (byte)0x01,//component ID, Y
                (byte)(1 << 4 | 1), //subsampling
                (byte)0x00,//Used quant table
            (byte)0x02,//component ID, Cb
                (byte)(subsamplingWidtht << 4 | subsamplingHeight), //subsampling
                (byte)0x01,//Used quant table
            (byte)0x03,//component ID, Cr
                (byte)(subsamplingWidtht << 4 | subsamplingHeight), //subsampling
                (byte)0x01,//Used quant table
    };
  }

  private byte[] getScanData(byte[] scanCb,byte[] scanCr,byte[] scanY) {
    //When creating this block of bytes we need to make sure that no 0xFF value shows up which would otherwise signal a frame
    byte[] returnArray = new byte[(scanCb.length + scanCr.length + scanY.length) * 2]; //We need extra space for replacing the 0xFF in the output
    int indexInReturnArray = 0;
    //Appens a 0x00 after a 0xFF for the scanCb
    for (int i = 0; i < scanCb.length; i++) {
      returnArray[indexInReturnArray++] = scanCb[i];
      if (scanCb[i] == -1) {
        returnArray[indexInReturnArray++] = 0x00;
      }
    }
    //Appens a 0x00 after a 0xFF for the scanCr
    for (int i = 0; i < scanCr.length; i++) {
      returnArray[indexInReturnArray++] = scanCr[i];
      if (scanCr[i] == -1) {
        returnArray[indexInReturnArray++] = 0x00;
      }
    }
    //Appens a 0x00 after a 0xFF for the scanY
    for (int i = 0; i < scanY.length; i++) {
      returnArray[indexInReturnArray++] = scanY[i];
      if (scanY[i] == -1) {
        returnArray[indexInReturnArray++] = 0x00;
      }
    }

    //Return only what has actually being used.
    return Arrays.copyOfRange(returnArray, 0, indexInReturnArray - 1);
  }

  private byte[] getEOI()
  {
    return new byte[]{(byte)0xFF, (byte)0xD9};
  }
}
