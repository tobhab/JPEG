package JPEG;

import java.io.*;

/**
 * Class for efficiently writing bits to a datastream.
 */

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

  /**
   * Writes a single bit into the stream.
   */
  public void write(boolean value) throws IOException {
    bits <<= 1;             //Make room on the right side to ...
    bits |= value ? 1 : 0;  // ...push in another bit there.
    storedBits++;
    writeFullBytes();
  }

  /**
   * Writes the 32 bits from the given int into the stream.
   */
  public void write(int values) throws IOException {
    write(values, 32);
  }

  /**
   * Writes the n rightmost bits into the stream
   * @param values Bit which are going to be saved into the stream, aligned to the right.
   * The most significat bit is on the left.
   * @param n Number of bits which are going to be saved into the stream.
   * @throws IOException
   */
  public void write(int values, int n) throws IOException {
    int andBy = ((1 << n) - 1);//Generate a mask for the lower bits, to...
    values = values & andBy;    //...zero the unused top bits to avoid data corruption
    bits <<= n;                 //Make room on the right side to ...
    bits |= values;             //...push new values in there
    storedBits += n;
    writeFullBytes();
  }

  /**
   * Checks if enough bits are puffered and then writes them out into the stream.
   */
  private void writeFullBytes() throws IOException {
    while (storedBits >= bitsStoredAtOnce) {
      storedBits -= bitsStoredAtOnce;
      int outputBits = (int) (bits >> storedBits);
      out.write(outputBits);
    }
  }

  /**
   * Writes the remaining bits into the stream and then closes the connection.
   * The unused bits which are on the right of the byte are padded with '1'-bits.
   */
  @Override
  public void close() throws IOException {
    if (storedBits != 0) {
      //save the remaining bits and do padding with 1 bits according to spec
      write(-1, bitsStoredAtOnce - (storedBits % bitsStoredAtOnce));
    }
    out.close();
  }
}
