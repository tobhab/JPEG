package JPEG;

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

  ZickZackInverse zickZackInverseY;
  ZickZackInverse zickZackInverseCb;
  ZickZackInverse zickZackInverseCr;

  EntropieEncoding entropyEncoding;

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
    System.out.println("The image is " + width + "x" + height + "(height x width)");
    System.out.println(" done");
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
    yCbCrConverter = new YCbCrConverter(imageReader.getRGBArray(), mMatrixYCbCr, aMatrixYCbCr);
    System.out.println(" done");
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
    subsampling = new Subsampling(yCbCrConverter.getYCbCrArray(), subsamplingType);
    System.out.println(" done");
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
   * Perform the (forward) Discrete Cosine Transform for each layer
   * 
   * @return this
   * @throws IOException
   */
  public JPEG dct() throws IOException {
    System.out.println("Performing Discrete Cosine Transform (DCT):");
    
    System.out.print("Y-Layer ...");
    dctY = new DCT(subsampling.getLayerY(), blockSize, "layerY");
    System.out.println(" done");
    
    System.out.print("Cb-Layer ...");
    dctCb = new DCT(subsampling.getLayerCb(), blockSize, "layerCb");
    System.out.println(" done");
    
    System.out.print("Cr-Layer ...");
    dctCr = new DCT(subsampling.getLayerCr(), blockSize, "layerCr");
    System.out.println(" done");
    
    System.out.println("DCT applied.");
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
    quantY = new Quantization(dctY.getDCTResult(), quantMatrixLum, "layerY");
    quantCb = new Quantization(dctCb.getDCTResult(), quantMatrixChro, "layerCb");
    quantCr = new Quantization(dctCr.getDCTResult(), quantMatrixChro, "layerCr");
    System.out.println(" done");
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
    dequantY = new Dequantization(zickZackInverseY.getResult(), quantMatrixLum, "layerY");
    dequantCb = new Dequantization(zickZackInverseCb.getResult(), quantMatrixChro, "layerCb");
    dequantCr = new Dequantization(zickZackInverseCr.getResult(), quantMatrixChro, "layerCr");
    //dequantY = new Dequantization(quantY.getQuantizationResult(), quantMatrixLum, "layerY");
    //dequantCb = new Dequantization(quantCb.getQuantizationResult(), quantMatrixChro, "layerCb");
    //dequantCr = new Dequantization(quantCr.getQuantizationResult(), quantMatrixChro, "layerCr");
    System.out.println(" done");
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
    idctY = new IDCT(dequantY.getDequantizationResult(), blockSize, "layerY");
    System.out.println(" done");
    
    System.out.print("Cb-Layer ...");

    idctCb = new IDCT(dequantCb.getDequantizationResult(), blockSize, "layerCb");
    System.out.println(" done");
    
    System.out.print("Cr-Layer ...");
    idctCr = new IDCT(dequantCr.getDequantizationResult(), blockSize, "layerCr");
    System.out.println(" done");
    
    System.out.println("IDCT applied.");
    return this;
  }

  /**
   * Reverse the forward Discrete Cosine Transform directly from DCT results
   * 
   * @return this
   * @throws IOException
   */
  public JPEG idctWithoutQuantization() throws IOException {
    System.out.println("Inverting Discrete Cosine Transform:");
    
    System.out.print("Y-Layer ...");
    idctY = new IDCT(dctY.getDCTResult(), blockSize, "layerY");
    System.out.println(" done");
    
    System.out.print("Cb-Layer ...");

    idctCb = new IDCT(dctCb.getDCTResult(), blockSize, "layerCb");
    System.out.println(" done");
    
    System.out.print("Cr-Layer ...");
    // funny colors by using Cb for Cr ;)... idctCr = new IDCT(dctCb.getDCTResult(), blockSize, "layerCr");
    idctCr = new IDCT(dctCr.getDCTResult(), blockSize, "layerCr");
    System.out.println(" done");
    
    System.out.println("IDCT applied.");
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
        idctY.getIDCTResult(),
        idctCb.getIDCTResult(),
        idctCr.getIDCTResult(),
        subsamplingType);
    System.out.println(" done");
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
    rgbConvert = new RGBConverter(revSubsampling.getYCbCr(), mMatrixRGB, aMatrixRGB);
    System.out.println(" done");
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
    imageWriter = new ImageWriter(rgbConvert.getRGBArray(), filePath);

    System.out.println(" done");
    timeEnd = System.currentTimeMillis();
    System.out.printf("Execution time: %2d seconds\n", (timeEnd - timeStart) / 1000);
    return this;
  }

  /**
   * DC Coding and Zig-Zag sequence. No inverse function implemented.
   * Separate the DC coefficient of coherent blocks and build differences
   *
   * @return this
   */
  public JPEG zickZack() throws Exception {
    System.out.print("Performing ZickZack-Encoding...");
    zickZackY = new ZickZack(quantY.getQuantizationResult(), blockSize);
    zickZackCb = new ZickZack(quantY.getQuantizationResult(), blockSize);
    zickZackCr = new ZickZack(quantY.getQuantizationResult(), blockSize);
    System.out.println(" done");
    return this;
  }

  /**
   * DC Coding and Zig-Zag sequence. No inverse function implemented.
   * Separate the DC coefficient of coherent blocks and build differences
   *
   * @return this
   */
  public JPEG inverseZickZack() throws Exception {
    System.out.print("Performing the inverse ZickZack-Encoding...");
    zickZackInverseY = new ZickZackInverse(zickZackY.getResult(), blockSize, width, height);
    zickZackInverseCb = new ZickZackInverse(zickZackCb.getResult(), blockSize, width, height);
    zickZackInverseCr = new ZickZackInverse(zickZackCr.getResult(), blockSize, width, height);
    System.out.println(" done");

    return this;
  }

  @Deprecated
  /**
   * Build a Huffman table for entropy coding. Not completed.
   * No inverse function implemented.
   * 
   * @return this
   */
  public JPEG entropyEncoding() {

    // join all zick-zack arrays of all layers
    int[][] zickZackArrays = {
        zickZackY.getResult(),
        zickZackCr.getResult(),
        zickZackCb.getResult(),
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

    entropyEncoding = new EntropieEncoding(result);

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
