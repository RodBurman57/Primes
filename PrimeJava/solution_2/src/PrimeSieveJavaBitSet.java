import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.Map;

// This tries to do better than java.util.BitSet but reatin single bit for storage
// Tried byte, int and long arrays as container and long is fastest - slightly
// bis/bic i.e. bit set/clear are a faster API than set with boolean value, so use that
public class PrimeSieveJavaBitSet {

  private static final int LIMIT = 1000000;
  private static final int TIME = 5;

  // Historical data for validating our results - the number of primes to be found under some
  // limit, such as 168 primes under 1000
  private static final Map<Integer, Integer> MY_DICT = Map.of( //
      10, 4, //
      1000, 168, //
      10000, 1229, //
      100000, 9592, //
      1000000, 78498, //
      10000000, 664579, //
      100000000, 5761455 //
  );

  // "local" bit set values
  private static final long[] BIS = {
	   0x0000000000000001L,  0x0000000000000002L,  0x0000000000000004L,  0x0000000000000008L,
	   0x0000000000000010L,  0x0000000000000020L,  0x0000000000000040L,  0x0000000000000080L,
	   0x0000000000000100L,  0x0000000000000200L,  0x0000000000000400L,  0x0000000000000800L,
	   0x0000000000001000L,  0x0000000000002000L,  0x0000000000004000L,  0x0000000000008000L,
	   0x0000000000010000L,  0x0000000000020000L,  0x0000000000040000L,  0x0000000000080000L,
	   0x0000000000100000L,  0x0000000000200000L,  0x0000000000400000L,  0x0000000000800000L,
	   0x0000000001000000L,  0x0000000002000000L,  0x0000000004000000L,  0x0000000008000000L,
	   0x0000000010000000L,  0x0000000020000000L,  0x0000000040000000L,  0x0000000080000000L,
	   0x0000000100000000L,  0x0000000200000000L,  0x0000000400000000L,  0x0000000800000000L,
	   0x0000001000000000L,  0x0000002000000000L,  0x0000004000000000L,  0x0000008000000000L,
	   0x0000010000000000L,  0x0000020000000000L,  0x0000040000000000L,  0x0000080000000000L,
	   0x0000100000000000L,  0x0000200000000000L,  0x0000400000000000L,  0x0000800000000000L,
	   0x0001000000000000L,  0x0002000000000000L,  0x0004000000000000L,  0x0008000000000000L,
	   0x0010000000000000L,  0x0020000000000000L,  0x0040000000000000L,  0x0080000000000000L,
	   0x0100000000000000L,  0x0200000000000000L,  0x0400000000000000L,  0x0800000000000000L,
	   0x1000000000000000L,  0x2000000000000000L,  0x4000000000000000L,  0x8000000000000000L
  };
  private static final long[] BIC = {
	   ~0x0000000000000001L,  ~0x0000000000000002L,  ~0x0000000000000004L,  ~0x0000000000000008L,
	   ~0x0000000000000010L,  ~0x0000000000000020L,  ~0x0000000000000040L,  ~0x0000000000000080L,
	   ~0x0000000000000100L,  ~0x0000000000000200L,  ~0x0000000000000400L,  ~0x0000000000000800L,
	   ~0x0000000000001000L,  ~0x0000000000002000L,  ~0x0000000000004000L,  ~0x0000000000008000L,
	   ~0x0000000000010000L,  ~0x0000000000020000L,  ~0x0000000000040000L,  ~0x0000000000080000L,
	   ~0x0000000000100000L,  ~0x0000000000200000L,  ~0x0000000000400000L,  ~0x0000000000800000L,
	   ~0x0000000001000000L,  ~0x0000000002000000L,  ~0x0000000004000000L,  ~0x0000000008000000L,
	   ~0x0000000010000000L,  ~0x0000000020000000L,  ~0x0000000040000000L,  ~0x0000000080000000L,
	   ~0x0000000100000000L,  ~0x0000000200000000L,  ~0x0000000400000000L,  ~0x0000000800000000L,
	   ~0x0000001000000000L,  ~0x0000002000000000L,  ~0x0000004000000000L,  ~0x0000008000000000L,
	   ~0x0000010000000000L,  ~0x0000020000000000L,  ~0x0000040000000000L,  ~0x0000080000000000L,
	   ~0x0000100000000000L,  ~0x0000200000000000L,  ~0x0000400000000000L,  ~0x0000800000000000L,
	   ~0x0001000000000000L,  ~0x0002000000000000L,  ~0x0004000000000000L,  ~0x0008000000000000L,
	   ~0x0010000000000000L,  ~0x0020000000000000L,  ~0x0040000000000000L,  ~0x0080000000000000L,
	   ~0x0100000000000000L,  ~0x0200000000000000L,  ~0x0400000000000000L,  ~0x0800000000000000L,
	   ~0x1000000000000000L,  ~0x2000000000000000L,  ~0x4000000000000000L,  ~0x8000000000000000L
  };
  private static final int[] CAR = setCar();

  private static final int[] setCar() {
    final int[] car = new int[256];
    for (int i = 0; i < car.length; i++) {
      car[i] = bitsInByte(i);
    }
    return car;
  }

  private static final int bitsInByte(final int val) {
    int count = 0;
    for (int i = 0; i < 8; i++) {
      if ((BIS[i] & val) != 0) { count++; }
    }
    return count;
  }

  public static void main(final String[] args) {
    var passes = 0;
    var sieve = new PrimeSieve(LIMIT);
    final var tStart = currentTimeMillis();

    while (MILLISECONDS.toSeconds(currentTimeMillis() - tStart) < TIME) {
      sieve = new PrimeSieve(LIMIT);
      sieve.runSieve();
      passes++;
    }

    sieve.printResults(false, MILLISECONDS.toSeconds(currentTimeMillis() - tStart), passes);
  }
  

  public static class PrimeSieve {

    private final int sieveSize;
    private final long[] longs;

    public PrimeSieve(final int size) {
      // Upper limit, highest prime we'll consider
      this.sieveSize = size;
      // since we filter evens, just half as many bits
      this.longs = new long[(size + 63) >> 6]; // need to round up
      set(0, sieveSize, true);
    }

    private void set(final int idx, final boolean val) {
      if (val) {
        bis(idx);
      } else {
        bic(idx);
      }
    }

    public void bis(final int idx) {
      longs[idx >> 6] |= BIS[idx & 63]; // set
    }

    public void bic(final int idx) {
      longs[idx >> 6] &= BIC[idx & 63]; // clear
    }

    public void set(final int first, final int len, final boolean val) {
      // skip to first bit - skip longs 
      int skip = first >> 6;
      final int bits0 = first & 63;
      if (bits0 > 0) {
        skip++;
        for (int i = 0; i < bits0; i++) {
          set(i, val);
        }
      }
      final int isize = len >> 6;
      final long lval = val ? 0xFFFFFFFFFFFFFFFFL : 0L;
      // flood fill
      for (int i = skip; i < isize; i++) {
        longs[i] = lval;
      }
      // left overs bits
      for (int i = (isize << 6); i < len; i++) {
        set(i, val);
      }
    }

    // Calculate the primes up to the specified limit
    public void runSieve() {
      var factor = 3;
      final var q = (int) (Math.sqrt(this.sieveSize) + 0.5); // round up

      while (factor < q) {
        for (var num = factor; num <= this.sieveSize; num++) {
          if (getBit(num)) {
            factor = num;
            break;
          }
        }

        // If marking factor 3, you wouldn't mark 6 (it's a mult of 2) so start with the 3rd
        // instance of this factor's multiple. We can then step by factor * 2 because every second
        // one is going to be even by definition
        for (var num = factor * 3; num <= this.sieveSize; num += factor * 2) {
          bic(num);
	}
        factor += 2; // No need to check evens, so skip to next odd (factor = 3, 5, 7, 9...)
      }
    }

    // Return the count of bits that are still set in the sieve. Assumes you've already called
    // runSieve, of course!
    public int countPrimes() {
      // not pretty but reasonably fast
      int count = 0;
      for (int i = 0; i < longs.length; i++) {
        final long uns64 = longs[i];
        count += CAR[(int)(uns64 & 0xFFL)];
        count += CAR[(int)((uns64 >>  8) & 0xFFL)];
        count += CAR[(int)((uns64 >> 16) & 0xFFL)];
        count += CAR[(int)((uns64 >> 24) & 0xFFL)];
        count += CAR[(int)((uns64 >> 32) & 0xFFL)];
        count += CAR[(int)((uns64 >> 40) & 0xFFL)];
        count += CAR[(int)((uns64 >> 48) & 0xFFL)];
        count += CAR[(int)((uns64 >> 56) & 0xFFL)];
      }
      return count;
    }

    // Look up our count of primes in the historical data (if we have it) to see if it matches
    public boolean validateResults() {
      if (MY_DICT.containsKey(this.sieveSize))
        return MY_DICT.get(this.sieveSize) == this.countPrimes();
      return false;
    }

    // Gets a bit from the array of bits, but automatically just filters out even numbers as
    // false, and then only uses half as many bits for actual storage
    private boolean getBit(final int index) {
      return (index % 2 == 0) ? false : ((longs[index >> 6] & BIS[index & 63]) != 0);
    }

    // Reciprocal of GetBit, ignores even numbers and just stores the odds. Since the prime sieve
    // work should never waste time clearing even numbers, this code will assert if you try to
    private void clearBit(final int index) {
      if (index % 2 == 0) {
        System.out.println("You are setting even bits, which is sub-optimal");
      }
      bic(index / 2);
    }

    // Displays the primes found (or just the total count, depending on what you ask for)
    public void printResults(final boolean showResults, final double duration, final long passes) {
      if (showResults)
        System.out.print("2, ");

      var count = 1;
      for (var num = 3; num <= this.sieveSize; num++) {
        if (this.getBit(num)) {
          if (showResults)
            System.out.printf("%d, ", String.valueOf(num));
          count++;
        }
      }
      if (showResults)
        System.out.println("");

      System.out.printf("Passes: %d, Time: %f, Avg: %f, Limit: %d, Count: %d, Valid: %s%n", passes,
          duration, duration / passes, sieveSize, count, this.validateResults());
      System.out.println();
      System.out.printf("PratimGhosh86;%d;%f;1;algorithm=base,faithful=yes,bits=1\n", passes,
          duration);
    }
  }

}
