class Token {
    int offset, length;
    char nextSymbol;

    public Token(int offset, int length, char nextSymbol) {
        this.offset = offset;
        this.length = length;
        this.nextSymbol = nextSymbol;
    }
}

public class LZ77 implements CompressionStrategy {

    private int[] kmp(String pat, String s) {
        if (s.isEmpty()) {
            return new int[0];
        }
        int[] f = new int[pat.length()];
        int l = 0;
        f[0] = 0;
        for (int i = 1; i < pat.length(); i++) {
            while (l > 0 && pat.charAt(i) != pat.charAt(l)) l = f[l - 1];
            if (pat.charAt(i) == pat.charAt(l))
                l++;
            f[i] = l;
        }
        int[] f2 = new int[s.length()];
        f2[0] = 0;
        l = 0;
        for (int i = 0; i < s.length(); i++) {
            while (l > 0 && s.charAt(i) != pat.charAt(l)) l = f[l - 1];
            if (s.charAt(i) == pat.charAt(l)) {
                l++;
            }

            f2[i] = l;
            if (l == pat.length()) {
                l = f[l - 1];
            }
        }

        return f2;
    }

    private Token find_longest_match(String text, int searchFrom, int matchFrom, int searchWindow) {
        String pat = text.substring(matchFrom, matchFrom + searchWindow);
        String s = text.substring(searchFrom, matchFrom);
        int[] f = kmp(pat, s);

        int offset = 0, length = 0;
        char nextSymbol = text.charAt(matchFrom);
        for (int i = s.length() - 1; i >= 0; i--) {
            if (f[i] > length) {
                int l = i - f[i] + 1;
                offset = matchFrom - l;
                length = f[i];
                nextSymbol = text.charAt(matchFrom + length);
            }
        }
        return new Token(offset, length, nextSymbol);
    }

    public String compress(String text) {
        StringBuilder sb = new StringBuilder();
        int bufferSize = 100, searchWindow = 100;
        for (int i = 0; i < text.length() - 1; ) {
            Token tkn = find_longest_match(text, Math.max(0, i - bufferSize), i, Math.min(searchWindow, text.length() - (i + 1)));
            i += tkn.length + 1;
            sb.append(String.format("%d, %d, %c%n", tkn.offset, tkn.length, tkn.nextSymbol));
        }
        return sb.toString();
    }


    public String decompress(String text) {
        StringBuilder sb = new StringBuilder();
        String[] tokens = text.split("\n");

        for (String token : tokens) {

            String[] parts = token.split(", ");

            int offset = Integer.parseInt(parts[0]);
            int length = Integer.parseInt(parts[1]);
            char character = parts[2].charAt(0);
            int start = sb.length() - offset, end = start + length;
            for (int i = start; i < end; i++) {
                sb.append(sb.charAt(i));
            }
            sb.append((character));
        }

        return sb.toString();
    }

}