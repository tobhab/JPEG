// Ausarbeitung im Fach Advanced Computer Networks (Master AI Hochschule Fulda)
// Autoren: S. Amelunxen, C. Hardegen, B. Pfuelb
// Erweiterungen (Manipulation Quantisierungsmatrix, IDCT ohne Quantisierung): S. Rieger 

import JPEG.*;

import static JPEG.Constants.*;
import static JPEG.SubsamplingType.*;

public class Main {

  final static int BLOCK_SIZE = 8;

  public static void main(String[] args) throws Exception {
    System.out.println("Starting compression algorithm:");

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
        .createHuffmanTables()
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

}
