import java.util.HashMap;

public class LZW implements CompressionStrategy {

    public String compress(String text) {
        String w = "";
        HashMap<String, Integer> dictionary = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        // initialize dictionary with all ascii characters
        for (char i = 0; i <= 255; i++) {
            dictionary.put("" + i, (int) i);
        }

        int index = 256;
        for (int i = 0; i < text.length(); i++) {
            String tmp = w + text.charAt(i);
            if (dictionary.containsKey(tmp)) {
                w = tmp;
            } else {
                dictionary.put(tmp, index);
                sb.append(String.format("%d%n", dictionary.get(w)));
                // reset
                index++;
                w = "" + text.charAt(i);
            }
        }
        sb.append(String.format("%d%n", dictionary.get(w)));
        return sb.toString();
    }

    public String decompress(String text) {
        StringBuilder sb = new StringBuilder();
        String[] tokens = text.split("\n");
        HashMap<Integer, String> dictionary = new HashMap<>();
        int curIndex = 256;

        // initialize dictionary with all ascii characters
        for (char i = 0; i <= 255; i++) {
            dictionary.put((int) i, "" + i);
        }

        String prev = "";
        for (String token : tokens) {
            int index = Integer.parseInt(token);
            String w = dictionary.get(index);
            if (!prev.isEmpty()) {
                if (w != null) {
                    dictionary.put(curIndex, prev + w.charAt(0));
                } else {
                    dictionary.put(curIndex, prev + prev.charAt(0));
                    w = dictionary.get(index);
                }
                curIndex++;
            }
            sb.append(w);
            prev = w;
        }

        return sb.toString();
    }

}
