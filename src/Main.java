// Ausarbeitung im Fach Advanced Computer Networks (Master AI Hochschule Fulda)
// Autoren: S. Amelunxen, C. Hardegen, B. Pfuelb
// Erweiterungen (Manipulation Quantisierungsmatrix, IDCT ohne Quantisierung): S. Rieger 

import JPEG.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import static JPEG.Constants.*;
import static JPEG.SubsamplingType.*;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;

public class Main {

  final static int BLOCK_SIZE = 8;

  public static void main(String[] args) throws Exception {

    boolean isTestingComponents = false;
    if(isTestingComponents) {
      java.util.Random random = new Random(); //Bad initialisation, but good enough
      int loopCounts = 10000;

      //testBitStreamClasses();

      while (loopCounts-- > 0) {
        testHuffman(random);
      }
    }

    System.out.println("Starting compression algorithm:");
    new JPEG()
        .readImage("Lenna016x016.png")
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
        .setDefaultHuffmanTables()
        .runlengthEncode()
        .huffmanEncode()
        .writeOutJpeg("myownjpeg.jpg")
        .huffmanDecode()
        .runlengthDecode()
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
   * Does a simple test to verify that the data that is written can also be read again correctly
   */
  private static void testHuffman(java.util.Random random) throws IOException {

    short[] acValues = JPEGHuffmanTable.StdDCChrominance.getValues();

    HuffmanTree treeAC = new HuffmanTree(
            JPEGHuffmanTable.StdDCChrominance.getLengths(),
            acValues);

    int numberOfBytesToTest = 1000000;


    int[] dataToTest = new int[numberOfBytesToTest];
    for (int i = 0; i < dataToTest.length; i++) {

      dataToTest[i] = acValues[random.nextInt(acValues.length)];
    }


    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BitStreamWriter writer = new BitStreamWriter(out);


    for (int i = 0; i < dataToTest.length; i++) {
      treeAC.writeCodeToWriter(writer, dataToTest[i]);
    }

    writer.close();
    byte[] array = out.toByteArray();

    ByteArrayInputStream in = new ByteArrayInputStream(array);
    BitStreamReader reader = new BitStreamReader(in);

    int[] testResults = new int[numberOfBytesToTest];

    for (int i = 0; i < dataToTest.length; i++) {
      testResults[i] = treeAC.lookUpCodeNumber(reader);
    }

    boolean gotAnError = false;
    for (int i = 0; i < testResults.length; i++) {
      if (dataToTest[i] != testResults[i]) {
        System.out.println("Got different results after huffman at index " + i);
        gotAnError = true;
      }
    }
    if(!gotAnError)
    {
      System.out.println("Passed the test.");
    }
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
