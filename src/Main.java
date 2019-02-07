// Ausarbeitung im Fach Advanced Computer Networks (Master AI Hochschule Fulda)
// Autoren: S. Amelunxen, C. Hardegen, B. Pfuelb
// Erweiterungen (Manipulation Quantisierungsmatrix, IDCT ohne Quantisierung): S. Rieger 

import JPEG.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

import static JPEG.Constants.*;
import static JPEG.SubsamplingType.*;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;

public class Main {

  final static int BLOCK_SIZE = 8;

  public static void main(String[] args) throws Exception {

    boolean isTestingComponents = false;
    if (isTestingComponents) {
      java.util.Random random = new Random(); //Bad initialisation, but good enough
      int loopCounts = 1;

      //testBitStreamClasses();
      testRunlength();

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
   * Does a simple test to manually verify that the data is encoded correctly
   */
  private static void testRunlength() throws IOException {
    int[] allEmpty = new int[64];
    RunlengthEncode allEmptyTest = new RunlengthEncode(allEmpty, 8, 1);
    int[] allEmpty2Blocks = new int[64 * 2];
    RunlengthEncode allEmptyTest2 = new RunlengthEncode( allEmpty2Blocks, 8, 2);

    int[] allOne = new int[64];
    Arrays.fill(allOne, 1);
    RunlengthEncode allOneTest = new RunlengthEncode(allOne, 8, 1);

    int[][] differentOnePositions = new int[64][];
    RunlengthEncode singleOne[] = new RunlengthEncode[64];
    for (int i = 0; i < 64; i++) {
      differentOnePositions[i] = new int[64];
      differentOnePositions[i][i] = 1;
      singleOne[i] = new RunlengthEncode(differentOnePositions[i], 8, 1);
    }

    int[][] differentOnePositionsFixedAt12 = new int[64][];
    RunlengthEncode differentOnePositionsFixedAt12RLE[] = new RunlengthEncode[64];
    for (int i = 0; i < 64; i++) {
      differentOnePositionsFixedAt12[i] = new int[64];
      differentOnePositionsFixedAt12[i][12] = 1;
      differentOnePositionsFixedAt12[i][i] = 1;
      differentOnePositionsFixedAt12RLE[i] = new RunlengthEncode(differentOnePositionsFixedAt12[i], 8, 1);
    }

    System.out.println("Please manualy check that it always ends with a single -1 and counts the zeros correctly and adds the zero runs (represented by a 17 in the runlength) if needed");
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
