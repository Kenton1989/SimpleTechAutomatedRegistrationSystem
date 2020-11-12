package cx2002grp2.stars.util;

import java.util.Objects;

/**
 * A immutable class representing a pair of objects.
 * <p>
 * This class assume that two values it contains are both immutable.
 * <p>
 * If the condition above does not satisfy, {@link Pair#hashCode()} may return
 * wrong result, because the hash code for Pair object will be calculated and
 * buffered when it is constructed.
 */
public class Pair<T1, T2> {
    /**
     * First value of the pair
     */
    private T1 val1;
    /**
     * Second value of the pair
     */
    private T2 val2;
    /**
     * buffered hashcode
     */
    private int hashCodeBuf;

    /**
     * Construct a pair containing two given immutable values.
     * <p>
     * hash code will be calculated and buffered during constructing
     * 
     * @param val1 first value
     * @param val2 second value
     */
    public Pair(T1 val1, T2 val2) {
        this.val1 = val1;
        this.val2 = val2;

        hashCodeBuf = Objects.hash(val1, val2);
    }

    /**
     * Get the first value in the pair.
     * 
     * @return the first value in the pair.
     */
    public T1 val1() {
        return this.val1;
    }

    /**
     * Get the second value in the pair.
     * 
     * @return the second value in the pair.
     */
    public T2 val2() {
        return this.val2;
    }

    /**
     * {@inheritDoc}
     * Two pairs are equal only if val1().equals(val1()) and val2().equals(val2()).
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (!(o instanceof Pair<?, ?>)) {
            return false;
        }

        Pair<?, ?> pair = (Pair<?, ?>) o;

        return Objects.equals(val1, pair.val1) && Objects.equals(val2, pair.val2);
    }

    @Override
    public int hashCode() {
        return hashCodeBuf;
    }
}