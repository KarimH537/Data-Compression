import java.nio.ByteBuffer;
import java.util.*;

class Matrix {

    public double a11, a12, a21, a22;
    public Matrix codeBook = null;

    public Matrix(double a11, double a12, double a21, double a22) {
        this.a11 = a11;
        this.a12 = a12;
        this.a21 = a21;
        this.a22 = a22;
    }


    public double getDis(Matrix b) {
        return Math.abs(this.a11 - b.a11) + Math.abs(this.a12 - b.a12) + Math.abs(this.a21 - b.a21) + Math.abs(this.a22 - b.a22);
    }
}


public class LBG implements Quantizer {
    int nCodeBook = 0;

    public LBG() {
    }

    public LBG(int nCodeBook) {
        this.nCodeBook = nCodeBook;
    }

    List<Matrix> getMatrices(int[][] image) {
        ArrayList<Matrix> matrices = new ArrayList<>();
        for (int i = 0; i < image.length; i += 2) {
            for (int j = 0; j < image[i].length; j += 2) {
                int a11 = image[i][j], a12 = 0, a21 = 0, a22 = 0;
                if (j + 1 < image[i].length) {
                    a12 = image[i][j + 1];
                }
                if (i + 1 < image.length) {
                    a21 = image[i + 1][j];
                }
                if (i + 1 < image.length && j + 1 < image[i].length) {
                    a22 = image[i + 1][j + 1];
                }
                matrices.add(new Matrix(a11, a12, a21, a22));
            }
        }
        return matrices;
    }

    Matrix getGroupAvg(List<Matrix> group) {
        int n = group.size();
        double a11 = 0, a12 = 0, a21 = 0, a22 = 0;
        for (Matrix matrix : group) {
            a11 += matrix.a11;
            a12 += matrix.a12;
            a21 += matrix.a21;
            a22 += matrix.a22;
        }
        return new Matrix(a11 / n, a12 / n, a21 / n, a22 / n);
    }

    List<Matrix> getCodeBookGroup(Matrix codeBook, List<Matrix> matrices) {
        ArrayList<Matrix> group = new ArrayList<>();
        for (Matrix matrix : matrices) {
            if (matrix.codeBook == codeBook) {
                group.add(matrix);
            }
        }
        return group;
    }

    void changeGroupCodeBook(Matrix codeBook, Matrix newCodeBook, List<Matrix> matrices) {
        for (Matrix matrix : matrices) {
            if (matrix.codeBook == codeBook) {
                matrix.codeBook = newCodeBook;
            }
        }
    }

    boolean assign(List<Matrix> codeBooks, List<Matrix> group) {
        boolean change = false;
        for (Matrix matrix : group) {
            Matrix bestCB = null;
            double dist = Integer.MAX_VALUE;
            for (Matrix codeBook : codeBooks) {
                double tmp = matrix.getDis(codeBook);
                if (bestCB == null || tmp < dist) {
                    dist = tmp;
                    bestCB = codeBook;
                }
            }
            change |= (matrix.codeBook != bestCB);
            matrix.codeBook = bestCB;
        }
        return change;
    }

    List<Matrix> updateCodeBooksToAverage(List<Matrix> codeBooks, List<Matrix> matrices) {
        List<Matrix> averages = new ArrayList<>();
        for (Matrix codeBook : codeBooks) {
            List<Matrix> group = getCodeBookGroup(codeBook, matrices);
            Matrix avg = getGroupAvg(group);
            changeGroupCodeBook(codeBook, avg, matrices);
            averages.add(avg);
        }
        return averages;
    }

    public List<Byte> compress(int[][] image) {
        List<Matrix> matrices = getMatrices(image);
        Queue<Matrix> q = new LinkedList<>();
        q.add(null);

        // Initiate with empty CB
        List<Matrix> codeBooks = new ArrayList<>();
        while (!q.isEmpty()) {
            Matrix current = q.poll();
            // Get group of the current CB
            List<Matrix> curGroup = getCodeBookGroup(current, matrices);
            Matrix curAvg = getGroupAvg(curGroup);

            // Split
            Matrix floor = new Matrix(Math.ceil(curAvg.a11 - 1), Math.ceil(curAvg.a12 - 1), Math.ceil(curAvg.a21 - 1), Math.ceil(curAvg.a22 - 1));
            Matrix ceil = new Matrix(Math.floor(curAvg.a11 + 1), Math.floor(curAvg.a12 + 1), Math.floor(curAvg.a21 + 1), Math.floor(curAvg.a22 + 1));
            codeBooks.add(floor);
            codeBooks.add(ceil);

            if (q.isEmpty() && codeBooks.size() < nCodeBook) {
                assign(codeBooks, matrices);
                codeBooks = updateCodeBooksToAverage(codeBooks, matrices);
                q.addAll(codeBooks);
                codeBooks.clear();
            }
        }
        while (assign(codeBooks, matrices)) {
            codeBooks = updateCodeBooksToAverage(codeBooks, matrices);
        }

        HashMap<Matrix, Integer> mp = new HashMap<>();
        for (int i = 0; i < codeBooks.size(); i++) {
            mp.put(codeBooks.get(i), i);
        }
        List<Byte> bytes = new ArrayList<>();
        bytes.add((byte) nCodeBook);
        for (Matrix cb : codeBooks) {
            bytes.add((byte) Math.round(cb.a11));
            bytes.add((byte) Math.round(cb.a12));
            bytes.add((byte) Math.round(cb.a21));
            bytes.add((byte) Math.round(cb.a22));
        }
        for (byte b : ByteBuffer.allocate(4).putInt((image[0].length + 1)/2).array()) {
            bytes.add(b);
        }
        for (Matrix matrix : matrices) {
            bytes.add((byte) ((int) mp.get(matrix.codeBook)));
        }
        return bytes;
    }

    public int[][] decompress(List<Byte> bytes) {
        int nCodeBooks = bytes.get(0);
        HashMap<Integer, Matrix> mp = new HashMap<>();
        int nxt = 1;
        for (int i = 0; i < nCodeBooks; i++) {
            int[] mat = new int[4];
            for (int k = 0; k < 4; k++) {
                mat[k] = bytes.get(nxt++) & 0xff;
            }
            mp.put(i, new Matrix(mat[0], mat[1], mat[2], mat[3]));
        }

        // read 4 bytes for integer
        byte[] widthBytes = new byte[4];
        for (int i = 0; i < 4; i++, nxt++) {
            widthBytes[i] = bytes.get(nxt);
        }
        int width = ByteBuffer.wrap(widthBytes).getInt();

        List<Matrix> matrices = new ArrayList<>();
        for (; nxt < bytes.size(); nxt++) {
            int idx = bytes.get(nxt);
            matrices.add(mp.get(idx));
        }
        int height = matrices.size() / width;

        // width and height has number of 2x2 matrices
        int[][] a = new int[height * 2][width * 2];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Matrix cur = matrices.get(width * i + j);
                a[2 * i][2 * j] = (int) cur.a11;
                a[2 * i][2 * j + 1] = (int) cur.a12;
                a[2 * i + 1][2 * j] = (int) cur.a21;
                a[2 * i + 1][2 * j + 1] = (int) cur.a22;
            }
        }
        return a;
    }
}