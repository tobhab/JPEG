package JPEG;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class BitStreamReader implements Closeable {

  InputStream in;

  //the next bit to read is at the left most position in this long
  private long bits = 0;
  private short storedBits = 0;

  public BitStreamReader(InputStream in) {
    this.in = in;
  }

  public boolean readBit() throws IOException {
    return readBits(1) == 1;
  }

  public int readBits(int count) throws IOException {
    ensureBuffer(count);
    storedBits -= count;
    int shiftBy = 64 - count;
    int bitsToReturn = (int) (bits >>> shiftBy);
    bits <<= count;
    return bitsToReturn;
  }

  public int readInt() throws IOException {
    return readBits(32);
  }

  private void ensureBuffer(int count) throws IOException {
    while (storedBits < count) {
      long nextBits = in.read(); //Discard all but the 8 rightmost bits
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
