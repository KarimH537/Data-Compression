import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class Predictive2D implements Quantizer {


    private int predict(int c, int a, int b) {
        int mn = Math.min(a, c);
        int mx = Math.max(a, c);
        if (b <= mn) {
            return mx;
        } else if (b >= mx) {
            return mn;
        } else {
            return a + c - b;
        }
    }

    private int getCode(int value) {
        int l = -255, r = -223, range = 32;
        for (int i = 0; i < 16; i++) {
            if (value >= l && value <= r) {
                return i;
            }
            l += range;
            r += range;
        }
        return 0;
    }

    public List<Byte> compress(int[][] image) {
        int width = image[0].length;
        int height = image.length;
        int[][] decoded = new int[height][width];
        int[][] diff = new int[height][width];
        int[][] quantizedDiff = new int[height][width];

        Map<Integer, Integer> Q_ = new HashMap<>();
        int l = -255, r = -223, range = 32;
        for (int i = 0; i < 16; i++) {
            int mid = (l + r + 1) / 2;
            Q_.put(i, mid);
            l += range;
            r += range;
        }

        for (int i = 0; i < width; i++) {
            quantizedDiff[0][i] = diff[0][i] = decoded[0][i] = image[0][i];
        }
        for (int i = 0; i < height; i++) {
            quantizedDiff[i][0] = diff[i][0] = decoded[i][0] = image[i][0];
        }


        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                decoded[i][j] = predict(decoded[i - 1][j], decoded[i][j - 1], decoded[i - 1][j - 1]);
                diff[i][j] = image[i][j] - decoded[i][j];
                int code = getCode(diff[i][j]);
                quantizedDiff[i][j] = code;
                decoded[i][j] += Q_.get(code);
                decoded[i][j] = Math.min(decoded[i][j], 255);
                decoded[i][j] = Math.max(decoded[i][j], 0);
            }
        }


        List<Byte> bytes = new ArrayList<>();
        for (byte b : ByteBuffer.allocate(4).putInt(width).array()) {
            bytes.add(b);
        }
        for (byte b : ByteBuffer.allocate(4).putInt(height).array()) {
            bytes.add(b);
        }

        for (int i = 0; i < 16; i++) {
            bytes.add((byte) i);
            bytes.add((byte) ((Q_.get(i) < 0) ? 1 : 0));
            bytes.add((byte) Math.abs(Q_.get(i)));
        }

        for (int i = 0; i < width; i++) {
            bytes.add((byte) image[0][i]);
        }
        for (int i = 1; i < height; i++) {
            bytes.add((byte) image[i][0]);
        }
        int count = 0;
        byte x = 0;
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                if (count == 0) {
                    x = (byte) quantizedDiff[i][j];
                } else {
                    x |= (byte) (quantizedDiff[i][j] << 4);
                    bytes.add(x);
                    x = 0;
                }
                count++;
                count %= 2;
            }
        }
        if (count == 1) {
            bytes.add(x);
        }
        return bytes;
    }

    public int[][] decompress(List<Byte> bytes) {
        int ptr = 0;

        byte[] widthBytes = new byte[4];
        for (int i = 0; i < 4; i++, ptr++) {
            widthBytes[i] = bytes.get(ptr);
        }
        int width = ByteBuffer.wrap(widthBytes).getInt();

        byte[] heightBytes = new byte[4];
        for (int i = 0; i < 4; i++, ptr++) {
            heightBytes[i] = bytes.get(ptr);
        }
        int height = ByteBuffer.wrap(heightBytes).getInt();

        Map<Integer, Integer> Q_ = new HashMap<>();
        for (int i = 0; i < 16; i++, ptr += 3) {
            int code = bytes.get(ptr);

            int sign = bytes.get(ptr + 1);
            int value = bytes.get(ptr + 2) & 0xff;

            Q_.put(code, (1 - sign) * value + (-sign) * value);
        }

        int[][] image = new int[height][width];
        for (int i = 0; i < width; i++, ptr++) {
            image[0][i] = bytes.get(ptr) & 0xff;
        }
        for (int i = 1; i < height; i++, ptr++) {
            image[i][0] = bytes.get(ptr) & 0xff;
        }

        int count = 0;
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                if (count % 2 == 0) {
                    int code = bytes.get(ptr) & 0x0f;
                    image[i][j] = predict(image[i - 1][j], image[i][j - 1], image[i - 1][j - 1]) + Q_.get(code);
                } else {
                    int code = (bytes.get(ptr) & 0xf0) >> 4;
                    image[i][j] = predict(image[i - 1][j], image[i][j - 1], image[i - 1][j - 1]) + Q_.get(code);
                    ptr++;
                }
                image[i][j] = Math.min(image[i][j], 255);
                image[i][j] = Math.max(image[i][j], 0);
                count++;
            }
        }

        return image;
    }
}