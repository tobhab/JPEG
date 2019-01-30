package JPEG;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class for efficiently reading bits from a datastream.
 */
public class BitStreamReader implements Closeable {

  InputStream in;

  //the next bit to read is at the left most position in this long
  private long bits = 0;
  private short storedBits = 0;

  public BitStreamReader(InputStream in) {
    this.in = in;
  }

  /**
   * Reads a single bit from the stream.
   */
  public boolean readBit() throws IOException {
    return readBits(1) == 1;
  }

  /**
   * Reads 32 bits into the returned int.
   */
  public int readInt() throws IOException {
    return readBits(32);
  }

  /**
   * Reads n bits from the datastream 
   * @param n Number of bits which are going to be read from the stream.
   * @return The bits are right aligned with the MSB on the left.
   */
  public int readBits(int count) throws IOException {
    ensureBuffer(count);
    storedBits -= count;
    int shiftBy = 64 - count;
    int bitsToReturn = (int) (bits >>> shiftBy);
    bits <<= count;
    return bitsToReturn;
  }

  /**
   * Ensured that there are at least n bits left in the stream and throws an exception if not enough bits are left.
   */
  private void ensureBuffer(int n) throws IOException {
    while (storedBits < n) {
      long nextBits = in.read(); //Discard all but the 8 rightmost bits, because only the last byte holds actual data
      if (nextBits == -1) {
        throw new IOException("Not enough bits left in stream");
      }
      storedBits += 8;
      bits |= nextBits << (64 - storedBits);
    }
  }

  @Override
  public void close() throws IOException {
    in.close();
  }
}
