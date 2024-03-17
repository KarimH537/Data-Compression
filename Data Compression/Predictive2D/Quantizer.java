import java.util.List;

public abstract interface Quantizer {
    public abstract List<Byte> compress(int[][] image);

    public abstract int[][] decompress(List<Byte> bytes);
}