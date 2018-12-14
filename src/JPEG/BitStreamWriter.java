package JPEG;

import java.io.*;

public class BitStreamWriter implements Closeable {

  OutputStream out;
  /*
   * Using a long, since when there are 7 bits already in the buffer,
   * then the user is still able to add an entire 32bit and that would lead to data loss.
   * The newest bit is always added to the right.
   */
  private long bits = 0;
  private short storedBits = 0;

  //Only use the 8 right most bits, since that is the definition in OutputStream
  private final int bitsStoredAtOnce = 8;

  public BitStreamWriter(OutputStream out) {
    this.out = out;
  }

  public void write(boolean value) throws IOException {
    bits <<= 1;             //Make room on the right side to ...
    bits |= value ? 1 : 0;  // ...push in another bit there.
    storedBits++;
    writeFullBytes();
  }

  public void write(int values) throws IOException {
    write(values, 32);
  }

  /**
   * Writes the n rightmost bits into the stream in the order MSB to LSB
   * @param values
   * @param n
   * @throws IOException
   */
  public void write(int values, int n) throws IOException {
    int andBy = ~((1 << n) - 1);//Generate a mask for the lower bits, to...
    values = values & andBy;    //...zero the unused top bits to avoid data corruption
    bits <<= n;                 //Make room on the right side to ...
    bits |= values;             //...push new values in there
    storedBits += n;
    writeFullBytes();
  }

  private void writeFullBytes() throws IOException {
    while (storedBits >= bitsStoredAtOnce) {
      storedBits -= bitsStoredAtOnce;
      int outputBits = (int) (bits >> storedBits);
      out.write(outputBits);
    }
  }

  @Override
  public void close() throws IOException {
    if (storedBits != 0) {
      //save the remaining bits and do padding with 0 bits
      write(0, bitsStoredAtOnce - (storedBits % bitsStoredAtOnce));
    }
    out.close();
  }
}
