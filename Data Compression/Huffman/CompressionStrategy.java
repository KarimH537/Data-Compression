import java.util.List;

public abstract interface CompressionStrategy {
    public abstract List<Byte> compress(String text);

    public abstract String decompress(List<Byte> text);
}