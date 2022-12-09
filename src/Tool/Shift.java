package Tool;

import java.util.Objects;

public class Shift {
    public boolean neg = false;
    public int bitCount;

    public Shift(boolean neg, int bitCount) {
        this.neg = neg;
        this.bitCount = bitCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(neg, bitCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Shift other = (Shift) o;
        return Objects.equals(this.neg, other.neg) &&
            Objects.equals(this.bitCount, other.bitCount);
    }
}
