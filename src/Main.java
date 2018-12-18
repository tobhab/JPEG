// Ausarbeitung im Fach Advanced Computer Networks (Master AI Hochschule Fulda)
// Autoren: S. Amelunxen, C. Hardegen, B. Pfuelb
// Erweiterungen (Manipulation Quantisierungsmatrix, IDCT ohne Quantisierung): S. Rieger 

import JPEG.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static JPEG.Constants.*;
import static JPEG.SubsamplingType.*;

public class Main {

  final static int BLOCK_SIZE = 8;

  public static void main(String[] args) throws Exception {
    System.out.println("Starting compression algorithm:");

    //testBitStreamClasses();

    new JPEG()
        .readImage("Lenna.png")
        .setMultiplicationMatrixYCbCr(YPbPrMatrix)
        .setAdditionMatrixYCbCr(YCbCrMatrix)
        .convertRGBToYCbCr()
        .setSubsamplingType(TYPE_4_4_4)
        .subsampling()
        .setBlockSize(BLOCK_SIZE)
        .dct()
        //.setLuminanceQuality(Q255)
        //.setChromaticQuality(Q255)
        .quantization()
        .differentalEncoding()
        .zickZack()
        .setDefaultHuffmanTable()
        //
        //// Manipulation der DCT Koeffizienten
        //
        //.manipulateQuantization(8, 8, 8, 8, 0)
        //.manipulateQuantization(9, 8, 1, 1, 255)
        //.manipulateQuantization(8, 15, 1, 1, 255)
        //.manipulateQuantization(15, 15, 1, 1, 255)
        //.saveQuantizationMatrix("layerY_manipulated.txt")
        .inverseZickZack()
        .inverseDifferentalEncoding()
        .dequantization()
        .idct()
        .reverseSubsampling()
        .setSubtractionnMatrixRGB(YCbCrMatrix)
        .setMultiplicationMatrixRGB(RGBMatrix1)
        .convertYCbCrToRGB()
        .writeImage("myLenna.bmp");
    System.out.println("Exiting execution.");
  }

  /**
   * Does a simple test to verify that the order in which the bits are written and read are consistent
   * @throws IOException
   */
  private static void testBitStreamClasses() throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BitStreamWriter testOut = new BitStreamWriter(out);
    testOut.write(42);
    for (int i = 0; i < 12; i++) {

      testOut.write(false);
      testOut.write(true);
    }
    testOut.write(50);
    for (int i = 0; i < 12; i++) {

      testOut.write(true);
      testOut.write(true);
    }
    testOut.write(50);
    testOut.close();
    byte[] array = out.toByteArray();

    ByteArrayInputStream in = new ByteArrayInputStream(array);
    BitStreamReader testIn = new BitStreamReader(in);
    boolean passed = testIn.readInt() == 42;
    for (int i = 0; i < 12; i++) {

      passed &= testIn.readBit() == false;
      passed &= testIn.readBit() == true;
    }
    passed &= testIn.readInt() == 50;
    for (int i = 0; i < 12; i++) {
      passed &= testIn.readBit() == true;
      passed &= testIn.readBit() == true;
    }
    passed &= testIn.readInt() == 50;
    if(!passed)
    {
      System.out.println("Writing and reading from the bit stream did not yield the same results");
    }
    else
    {
      System.out.println("Writing and reading from the bitstream worked as planned");
    }
  }

}
