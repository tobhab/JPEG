package JPEG;

import javax.imageio.plugins.jpeg.JPEGHuffmanTable;

import static JPEG.Constants.*;
import static JPEG.SubsamplingType.*;
import java.io.IOException;

/**
 * Wrapper class for all steps of the JPEG process.
 * It provides methods to parameterize the single steps and execute it.
 *
 */
public class JPEG {

  public static boolean writeDebugFiles = false;

  private ImageReader imageReader;

  private double[][] mMatrixYCbCr;
  private double[][] aMatrixYCbCr;
  private YCbCrConverter yCbCrConverter;

  private SubsamplingType subsamplingType;
  private Subsampling subsampling;

  private int blockSize;
  private int blockCount;
  private int[][] quantMatrixLum;
  private int[][] quantMatrixChro;

  DCT dctY;
  DCT dctCb;
  DCT dctCr;

  Quantization quantY;
  Quantization quantCb;
  Quantization quantCr;

  ZickZack zickZackY;
  ZickZack zickZackCb;
  ZickZack zickZackCr;

  DifferentialEncoding differental_Y;
  DifferentialEncoding differental_Cb;
  DifferentialEncoding differental_Cr;

  RunlengthEncode runlengthEncode_Y;
  RunlengthEncode runlengthEncode_Cb;
  RunlengthEncode runlengthEncode_Cr;

  RunlengthDecode runlengthDecode_Y;
  RunlengthDecode runlengthDecode_Cb;
  RunlengthDecode runlengthDecode_Cr;

  InverseDifferentialEncoding inverseDifferental_Y;
  InverseDifferentialEncoding inverseDifferental_Cb;
  InverseDifferentialEncoding inverseDifferental_Cr;

  ZickZackInverse zickZackInverseY;
  ZickZackInverse zickZackInverseCb;
  ZickZackInverse zickZackInverseCr;

  HuffmanTree huffmanTreeAcY;
  HuffmanTree huffmanTreeDcY;
  HuffmanTree huffmanTreeAcCx;
  HuffmanTree huffmanTreeDcCx;

  HuffmanEncoding huffmanEncoding_Y;
  HuffmanEncoding huffmanEncoding_Cb;
  HuffmanEncoding huffmanEncoding_Cr;

  HuffmanDecoding huffmanDecoding_Y;
  HuffmanDecoding huffmanDecoding_Cb;
  HuffmanDecoding huffmanDecoding_Cr;

  Dequantization dequantY;
  Dequantization dequantCb;
  Dequantization dequantCr;

  IDCT idctY;
  IDCT idctCb;
  IDCT idctCr;

  ReverseSubsampling revSubsampling;

  private double[][] mMatrixRGB;
  private double[][] aMatrixRGB;

  RGBConverter rgbConvert;

  ImageWriter imageWriter;

  long timeStart;
  long timeEnd;

  int width;
  int height;

  double[][] lastResult2d_Y;
  double[][] lastResult2d_Cb;
  double[][] lastResult2d_Cr;

  double[][][] lastResult3d;

  byte[] lastResult1b_Y;
  byte[] lastResult1b_Cb;
  byte[] lastResult1b_Cr;

  int[] lastResult1i_Y;
  int[] lastResult1i_Cb;
  int[] lastResult1i_Cr;

  int[][] lastResult2i_Y;
  int[][] lastResult2i_Cb;
  int[][] lastResult2i_Cr;

  int[][][] lastResult3i;

  /**
   * Default constructor with default values
   */
  public JPEG() {
    setMultiplicationMatrixYCbCr(YPbPrMatrix);
    setAdditionMatrixYCbCr(YCbCrMatrix);
    setSubsamplingType(TYPE_4_2_2);
    setBlockSize(8);
    setLuminanceQuality(Q50);
    setChromaticQuality(Q50);
    setSubtractionnMatrixRGB(YCbCrMatrix);
    setMultiplicationMatrixRGB(RGBMatrix1);
  }

  /**
   * Read an image from file system by the given path and start time measurement
   * 
   * @param imagePath
   * @return this
   * @throws Exception
   */
  public JPEG readImage(String imagePath) throws Exception {
    timeStart = System.currentTimeMillis();

    System.out.print("Read image file ...");
    if (imageReader != null)
      throw new Exception("Error: Image already read!");

    imageReader = new ImageReader(imagePath);
    width = imageReader.getRGBArray()[0].length;
    height = imageReader.getRGBArray().length;
    setBlockCount((width / blockSize) * (height / blockSize));
    System.out.println("The image is " + width + "x" + height + "(height x width)");
    System.out.println("There are " + blockCount + " blocks in the image");
    System.out.println(" done");

    lastResult3i = imageReader.getRGBArray();
    return this;
  }

  /**
   * Set multiplication matrix for color space conversion RGB to YCbCr
   * 
   * @param mMatrix
   * @return this
   */
  public JPEG setMultiplicationMatrixYCbCr(double[][] mMatrix) {
    this.mMatrixYCbCr = mMatrix;
    return this;
  }

  /**
   * Set addition matrix for color space conversion RGB/YPbPr to YCbCr
   * 
   * @param aMatrix
   * @return this
   */
  public JPEG setAdditionMatrixYCbCr(double[][] aMatrix) {
    this.aMatrixYCbCr = aMatrix;
    return this;
  }

  /**
   * Convert color space by matrix multiplication with subsequent matrix addition with preset matrices and
   * write output images to the file system (local project directory).
   * 
   * @return this
   * @throws IOException
   */
  public JPEG convertRGBToYCbCr() throws IOException {
    System.out.print("Converting RGB to YCbCr ...");
    yCbCrConverter = new YCbCrConverter(lastResult3i, mMatrixYCbCr, aMatrixYCbCr);
    System.out.println(" done");
    lastResult3d = yCbCrConverter.getYCbCrArray();
    return this;
  }

  /**
   * Set subsampling type from enumeration SubsamplingType
   * 
   * @param subsamplingType
   * @return this
   */
  public JPEG setSubsamplingType(SubsamplingType subsamplingType) {
    this.subsamplingType = subsamplingType;
    return this;
  }

  /**
   * Create the subsampled versions of the specific color layers depending on chosen subsamplingType and 
   * write output images to the file system (local project directory).
   * 
   * @param subsamplingType
   * @return this
   */
  public JPEG subsampling() throws IOException {
    System.out.print("Performing subsampling ...");
    subsampling = new Subsampling(lastResult3d, subsamplingType);
    System.out.println(" done");
    lastResult2d_Y = subsampling.getLayerY();
    lastResult2d_Cb = subsampling.getLayerCb();
    lastResult2d_Cr = subsampling.getLayerCr();
    return this;
  }

  /**
   * Set the block size
   * 
   * @param block_size
   * @return this
   */
  public JPEG setBlockSize(int block_size) {
    this.blockSize = block_size;
    return this;
  }

  /**
   * Set the block count
   *
   * @param block_count
   * @return this
   */
  public JPEG setBlockCount(int block_count) {
    this.blockCount = block_count;
    return this;
  }

  /**
   * Perform the (forward) Discrete Cosine Transform for each layer
   * 
   * @return this
   * @throws IOException
   */
  public JPEG dct() throws IOException {
    System.out.println("Performing Discrete Cosine Transform (DCT):");
    
    System.out.print("Y-Layer ...");
    dctY = new DCT(lastResult2d_Y, blockSize, "layerY");
    System.out.println(" done");
    
    System.out.print("Cb-Layer ...");
    dctCb = new DCT(lastResult2d_Cb, blockSize, "layerCb");
    System.out.println(" done");
    
    System.out.print("Cr-Layer ...");
    dctCr = new DCT(lastResult2d_Cr, blockSize, "layerCr");
    System.out.println(" done");
    
    System.out.println("DCT applied.");

    lastResult2d_Y = dctY.getDCTResult();
    lastResult2d_Cb = dctCb.getDCTResult();
    lastResult2d_Cr = dctCr.getDCTResult();
    return this;
  }

  /**
   * Set the quantization matrix for luminance quantization
   * 
   * @param qualityMatrix
   * @return this
   */
  public JPEG setLuminanceQuality(int[][] qualityMatrix) {
    quantMatrixLum = qualityMatrix;
    return this;
  }

  /**
   * Set the quantization matrix for chromatic (Cb, Cr) quantization
   * 
   * @param qualityMatrix
   * @return this
   */
  public JPEG setChromaticQuality(int[][] qualityMatrix) {
    quantMatrixChro = qualityMatrix;
    return this;
  }

  /**
   * Quantize all layers with the previous selected quantization matrices
   * 
   * @return this
   * @throws IOException
   */
  public JPEG quantization() throws IOException {
    System.out.print("Performing quantization ...");
    quantY = new Quantization(lastResult2d_Y, quantMatrixLum, "layerY");
    quantCb = new Quantization(lastResult2d_Cb, quantMatrixChro, "layerCb");
    quantCr = new Quantization(lastResult2d_Cr, quantMatrixChro, "layerCr");
    System.out.println(" done");
    lastResult2i_Y = quantY.getQuantizationResult();
    lastResult2i_Cb = quantCb.getQuantizationResult();
    lastResult2i_Cr = quantCr.getQuantizationResult();
    return this;
  }

  /**
   * Reverse the quantization step
   * 
   * @return this
   * @throws IOException
   */
  public JPEG dequantization() throws IOException {
    System.out.print("Performing dequantization ...");
    dequantY = new Dequantization(lastResult2i_Y, quantMatrixLum, "layerY");
    dequantCb = new Dequantization(lastResult2i_Cb, quantMatrixChro, "layerCb");
    dequantCr = new Dequantization(lastResult2i_Cr, quantMatrixChro, "layerCr");
    System.out.println(" done");
    lastResult2d_Y = dequantY.getDequantizationResult();
    lastResult2d_Cb = dequantCb.getDequantizationResult();
    lastResult2d_Cr = dequantCr.getDequantizationResult();
    return this;
  }

  /**
   * Reverse the forward Discrete Cosine Transform
   * 
   * @return this
   * @throws IOException
   */
  public JPEG idct() throws IOException {
    System.out.println("Inverting Discrete Cosine Transform:");
    
    System.out.print("Y-Layer ...");
    idctY = new IDCT(lastResult2d_Y, blockSize, "layerY");
    System.out.println(" done");
    
    System.out.print("Cb-Layer ...");

    idctCb = new IDCT(lastResult2d_Cb, blockSize, "layerCb");
    System.out.println(" done");
    
    System.out.print("Cr-Layer ...");
    idctCr = new IDCT(lastResult2d_Cr, blockSize, "layerCr");
    System.out.println(" done");
    
    System.out.println("IDCT applied.");
    lastResult2d_Y = idctY.getIDCTResult();
    lastResult2d_Cb = idctCb.getIDCTResult();
    lastResult2d_Cr = idctCr.getIDCTResult();
    return this;
  }

  /**
   * Reverse the subsampling step by the given subsamplingType
   * write output images to the file system (local project directory).
   * 
   * @return this
   * @throws IOException
   */
  public JPEG reverseSubsampling() throws IOException {
    System.out.print("Inverting subsampling ...");
    revSubsampling = new ReverseSubsampling(
            lastResult2d_Y,
            lastResult2d_Cb,
            lastResult2d_Cr,
        subsamplingType);
    System.out.println(" done");
    lastResult3d = revSubsampling.getYCbCr();
    return this;
  }

  /**
   * Set multiplication matrix for color space conversion YCbCr to RGB.
   * 
   * @param mMatrixRGB
   * @return
   */
  public JPEG setMultiplicationMatrixRGB(double[][] mMatrixRGB) {
    this.mMatrixRGB = mMatrixRGB;
    return this;
  }

  /**
   * Set addition matrix for color space conversion YCbCr (YPbPr) to RGB.
   * 
   * @param mMatrixRGB
   * @return
   */
  public JPEG setSubtractionnMatrixRGB(double[][] aMatrixRGB) {
    this.aMatrixRGB = aMatrixRGB;
    return this;
  }

  /**
   * Convert color space by matrix addition with subsequent multiplication with preset matrices and
   * write output images to the file system (local project directory).
   * 
   * @return
   */
  public JPEG convertYCbCrToRGB() {
    System.out.print("Converting YCbCr to RGB ...");
    rgbConvert = new RGBConverter(lastResult3d, mMatrixRGB, aMatrixRGB);
    System.out.println(" done");
    lastResult3i = rgbConvert.getRGBArray();
    return this;
  }

  /**
   * Write the resulting image of the JPEG process to file system (local project directory) as BMP
   * 
   * @param filePath
   * @return this
   * @throws IOException
   */
  public JPEG writeImage(String filePath) throws IOException {
    System.out.printf("Writing all outputs to \"%s%s\" ...", System.getProperty("user.dir"), "\\");
    imageWriter = new ImageWriter(lastResult3i, filePath);

    System.out.println(" done");
    timeEnd = System.currentTimeMillis();
    System.out.printf("Execution time: %2d seconds\n", (timeEnd - timeStart) / 1000);
    return this;
  }

  /**
   * Perform the differential encoding
   *
   * @return this
   */
  public JPEG differentalEncoding() throws Exception {
    System.out.print("Performing DifferentialEncoding...");
    differental_Y = new DifferentialEncoding(lastResult2i_Y, blockSize);
    differental_Cb = new DifferentialEncoding(lastResult2i_Cb, blockSize);
    differental_Cr = new DifferentialEncoding(lastResult2i_Cr, blockSize);
    System.out.println(" done");
    lastResult2i_Y = differental_Y.getResult();
    lastResult2i_Cb = differental_Cb.getResult();
    lastResult2i_Cr = differental_Cr.getResult();
    return this;
  }

  /**
   * Perform the zickzack-encoding to convert the 2d imagine into a 1d stream of data
   *
   * @return this
   */
  public JPEG zickZack() throws Exception {
    System.out.print("Performing ZickZack-Encoding...");
    zickZackY = new ZickZack(lastResult2i_Y, blockSize);
    zickZackCb = new ZickZack(lastResult2i_Cb, blockSize);
    zickZackCr = new ZickZack(lastResult2i_Cr, blockSize);
    System.out.println(" done");
    lastResult1i_Y = zickZackY.getResult();
    lastResult1i_Cb = zickZackCb.getResult();
    lastResult1i_Cr = zickZackCr.getResult();
    return this;
  }

  public JPEG runlengthEncode() {
    System.out.print("Performing Runlength-Encoding...");
    runlengthEncode_Y = new RunlengthEncode(lastResult1i_Y, blockSize);
    runlengthEncode_Cb = new RunlengthEncode(lastResult1i_Cb, blockSize);
    runlengthEncode_Cr = new RunlengthEncode(lastResult1i_Cr, blockSize);
    System.out.println(" done");
    lastResult1i_Y = runlengthEncode_Y.getResult();
    lastResult1i_Cb = runlengthEncode_Cb.getResult();
    lastResult1i_Cr = runlengthEncode_Cr.getResult();
    return this;
  }

  public JPEG setHuffmanTable(short[] lengths, short[] values, boolean isLuminance, boolean isDCValues) {
    HuffmanTree table = new HuffmanTree(
            lengths,
            values);
    System.out.print("Setting the huffman table for ");
    if(isLuminance)
    {
      System.out.print("Luminance ");
      if(isDCValues)
      {
        huffmanTreeDcY = table;
        System.out.print("DC ");
      }
      else
      {
        huffmanTreeAcY = table;
        System.out.print("AC ");
      }
    }
    else
    {
      System.out.print("Chrominance ");
      if(isDCValues)
      {
        huffmanTreeDcCx = table;
        System.out.print("DC ");
      }
      else
      {
        huffmanTreeAcCx = table;
        System.out.print("AC ");
      }
    }
    System.out.println("to the given value");
    return this;
  }

  public JPEG setDefaultHuffmanTables() {
    System.out.println("Setting the default huffman table");
    setHuffmanTable(JPEGHuffmanTable.StdACChrominance.getLengths(), JPEGHuffmanTable.StdACChrominance.getValues(), false, false);
    setHuffmanTable(JPEGHuffmanTable.StdDCChrominance.getLengths(), JPEGHuffmanTable.StdDCChrominance.getValues(), false, true);
    setHuffmanTable(JPEGHuffmanTable.StdACLuminance.getLengths(), JPEGHuffmanTable.StdACLuminance.getValues(), true, false);
    setHuffmanTable(JPEGHuffmanTable.StdDCLuminance.getLengths(), JPEGHuffmanTable.StdDCLuminance.getValues(), true, true);
    return this;
  }

  public JPEG huffmanEncode() {
    try {
      System.out.print("Performing Huffman-Encoding...");
      huffmanEncoding_Y = new HuffmanEncoding(lastResult1i_Y, huffmanTreeAcY, huffmanTreeDcY);
      huffmanEncoding_Cb = new HuffmanEncoding(lastResult1i_Cb, huffmanTreeAcCx, huffmanTreeDcCx);
      huffmanEncoding_Cr = new HuffmanEncoding(lastResult1i_Cr, huffmanTreeAcCx, huffmanTreeDcCx);
      lastResult1b_Y = huffmanEncoding_Y.getResult();
      lastResult1b_Cb = huffmanEncoding_Cb.getResult();
      lastResult1b_Cr = huffmanEncoding_Cr.getResult();

      System.out.println(" done");
    } catch (IOException e) {
      e.printStackTrace();
    }


    return this;
  }

  public JPEG huffmanDecode() {
    try {
      System.out.print("Performing Huffman-Decoding...");

      huffmanDecoding_Y = new HuffmanDecoding(lastResult1b_Y, blockCount, blockSize, huffmanTreeAcY, huffmanTreeDcY, runlengthEncode_Y.getResult());
      huffmanDecoding_Cb = new HuffmanDecoding(lastResult1b_Cb, blockCount, blockSize, huffmanTreeAcCx, huffmanTreeDcCx, runlengthEncode_Cb.getResult());
      huffmanDecoding_Cr = new HuffmanDecoding(lastResult1b_Cr, blockCount, blockSize, huffmanTreeAcCx, huffmanTreeDcCx, runlengthEncode_Cr.getResult());
      lastResult1i_Y = huffmanDecoding_Y.getResult();
      lastResult1i_Cb = huffmanDecoding_Cb.getResult();
      lastResult1i_Cr = huffmanDecoding_Cr.getResult();
      System.out.println(" done");
    } catch (IOException e) {
      e.printStackTrace();
    }


    return this;
  }

  public JPEG runlengthDecode() {
    System.out.print("Performing Runlength-Decoding...");
    runlengthDecode_Y = new RunlengthDecode(lastResult1i_Y, blockSize, blockCount);
    runlengthDecode_Cb = new RunlengthDecode(lastResult1i_Cb, blockSize, blockCount);
    runlengthDecode_Cr = new RunlengthDecode(lastResult1i_Cr, blockSize, blockCount);
    System.out.println(" done");
    lastResult1i_Y = runlengthDecode_Y.getResult();
    lastResult1i_Cb = runlengthDecode_Cb.getResult();
    lastResult1i_Cr = runlengthDecode_Cr.getResult();
    return this;
  }

  /**
   * Inverse the zickzack-encoding to get back the 2d image data from the 1d data stream
   *
   * @return this
   */
  public JPEG inverseZickZack() throws Exception {
    System.out.print("Performing the inverse ZickZack-Encoding...");
    zickZackInverseY = new ZickZackInverse(lastResult1i_Y, blockSize, width, height);
    zickZackInverseCb = new ZickZackInverse(lastResult1i_Cb, blockSize, width, height);
    zickZackInverseCr = new ZickZackInverse(lastResult1i_Cr, blockSize, width, height);
    System.out.println(" done");
    lastResult2i_Y = zickZackInverseY.getResult();
    lastResult2i_Cb = zickZackInverseCb.getResult();
    lastResult2i_Cr = zickZackInverseCr.getResult();
    return this;
  }

  /**
   * Perform the inverse differential encoding
   *
   * @return this
   */
  public JPEG inverseDifferentalEncoding() throws Exception {
    System.out.print("Performing InverseDifferentialEncoding...");
    inverseDifferental_Y = new InverseDifferentialEncoding(lastResult2i_Y, blockSize);
    inverseDifferental_Cb = new InverseDifferentialEncoding(lastResult2i_Cb, blockSize);
    inverseDifferental_Cr = new InverseDifferentialEncoding(lastResult2i_Cr, blockSize);
    System.out.println(" done");
    lastResult2i_Y = inverseDifferental_Y.getResult();
    lastResult2i_Cb = inverseDifferental_Cb.getResult();
    lastResult2i_Cr = inverseDifferental_Cr.getResult();
    return this;
  }

  /**
   * Build a Huffman table for entropy coding. Not completed.
   * No inverse function implemented.
   * 
   * @return this
   */
  @Deprecated
  public JPEG createHuffmanTables() {

    // join all zick-zack arrays of all layers
    int[][] zickZackArrays = {
            lastResult1i_Y,
            lastResult1i_Cb,
            lastResult1i_Cr,
    };

    int length = 0;
    for (int[] arr : zickZackArrays) {
      length += arr.length;
    }

    int[] result = new int[length];
    int offset = 0;
    for (int[] arr : zickZackArrays) {
      System.arraycopy(arr, 0, result, offset, arr.length);
      offset += arr.length;
    }

    huffmanTreeAcCx = new HuffmanTree(result);
    return this;
  }

  /**
   * Manipulate Quantization Matrix.
   * 
   * @param x
   * @param y
   * @param value
   * @return
   */
  public JPEG manipulateQuantization(int y, int x, int height, int width, int value) {
      System.out.print("Manipulating quantization ...");
	  for (int ty=0; ty<height; ty++)
	  {
		  for (int tx=0; tx<width; tx++)
		  {
			  this.quantY.setQuantizationValue(y+ty, x+tx, value);
			  //this.quantCb.setQuantizationValue(y+ty, x+tx, value);
			  //this.quantCr.setQuantizationValue(y+ty, x+tx, value);
		  }
	  }
      System.out.println("done");

    return this;
  }  

  /**
   * Manipulate Quantization Matrix.
   * 
   * @param x
   * @param y
   * @param value
   * @return
 * @throws IOException 
   */
  public JPEG saveQuantizationMatrix(String fileName) throws IOException {
      System.out.print("Saving quantization matrix ...");
      this.quantY.saveQuantizationMatrix(fileName);
      System.out.println("done");

    return this;
  }  
  
}
