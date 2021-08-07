public class Bits {
    private static final byte[] BIS = { 0x1,  0x2,  0x4,  0x8,  0x10,  0x20,  0x40, (byte) 0x80};
    private static final byte[] BIC = {~0x1, ~0x2, ~0x4, ~0x8, ~0x10, ~0x20, ~0x40, (byte) ~0x80};
    private static final int[] CAR = setCar();
  
    private static final int[] setCar() {
      final int[] car = new int[256];
      for (int i = 0; i < car.length; i++) {
        car[i] = countBits(i);
      }
      return car;
    }
  
    private static final int countBits(final int val) {
      int count = 0;
      for (int i = 0; i < 8; i++) {
        if ((BIS[i] & val) != 0) { count++; }
      }
      return count;
    }
  
    private final int size;
    private final byte[] bytes;
  
    public Bits(final int size) {
      this.size = size;
      this.bytes = new byte[(size + 7) >> 3]; // need to round up
    }
  
    public boolean get(final int idx) {
      return (bytes[idx >> 3] & BIS[idx & 7]) != 0;
    }
  
    public void set(final int idx, final boolean val) {
      if (val) {
        bis(idx);
      } else {
        bic(idx);
      }
    }
  
    public void bis(final int idx) {
      bytes[idx >> 3] |= BIS[idx & 7]; // set
    }
  
    public void bic(final int idx) {
      bytes[idx >> 3] &= BIC[idx & 7]; // clear
    }
  
    public void set(final int first, final int len, final boolean val) {
       // skip to first bit - skip bytes 
       int skip = first >> 3;
       final int bits0 = first & 7;
       if (bits0 > 0) {
         skip++;
         for (int i = 0; i < bits0; i++) {
        set(i, val);
         }
       }
       final int bsize = len >> 3;
       final byte bval = val ? (byte) 0xFF : 0;
       // flood fill
       for (int i = skip; i < bsize; i++) {
         bytes[i] = bval;
       }
       // left overs bits
       final int left = len & 7;
       for (int i = 0; i < left; i++) {
          set(bytes[bval + 1], val);
       }
    }
  
    public int cardinality() {
      int count = 0;
      for (int i = 0; i < bytes.length; i++) {
        final int uns8 = bytes[i];
        count += CAR[uns8 & 0xFF];
      }
      return count;
    }
  }
  