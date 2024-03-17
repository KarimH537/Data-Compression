public abstract interface CompressionStrategy {
    public abstract String compress(String text);
    public abstract String decompress(String line);
}