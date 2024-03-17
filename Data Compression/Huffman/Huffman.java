import java.util.*;

class Node {
    char character;
    int frequency;
    Node left = null, right = null;

    // constructor
    public Node(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    public int getFrequency() {
        return this.frequency;
    }
}

public class Huffman implements CompressionStrategy {
    public void buildCode(String[] st, Node x, String s) {
        if (x.left != null && x.right != null) {
            buildCode(st, x.left, s + '0');
            buildCode(st, x.right, s + '1');
        } else {
            st[x.character] = s;
        }
    }

    private Node buildTree(HashMap<Character, Integer> freq) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparing(Node::getFrequency));
        for (Character k : freq.keySet()) {
            Node n = new Node(k, freq.get(k));
            pq.add(n);
        }
        Node root = null;
        while (pq.size() > 1) {
            Node x = pq.poll(), y = pq.poll();
            Node res = new Node('$', x.getFrequency() + y.getFrequency());
            res.left = x;
            res.right = y;
            root = res;
            pq.add(res);
        }
        return root;
    }

    public List<Byte> compress(String text) {
        HashMap<Character, Integer> freq = new HashMap<>();
        for (int i = 0; i < text.length(); i++) {
            freq.put(text.charAt(i), freq.getOrDefault(text.charAt(i), 0) + 1);
        }
        Node root = buildTree(freq);

        // Get the code of each character
        String[] st = new String[256];
        buildCode(st, root, "");

        List<Byte> bytes = new ArrayList<Byte>();
        bytes.add((byte) freq.size());

        // Save the lookup table
        for (Map.Entry<Character, Integer> entry : freq.entrySet()) {
            Character c = entry.getKey();
            Integer f = entry.getValue();

            bytes.add((byte) c.charValue());
            bytes.add(f.byteValue());
        }

        for (int i = 0; i < 256; i++) {
            String code = st[i];
            if (code == null) continue;
        }

        // Build the compressed string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String code = st[text.charAt(i)];
            sb.append(code);
        }
        while (sb.length() % 8 != 0) sb.append(0);

        // Save compressed string
        String compressed = sb.toString();
        for (int i = 0; i < compressed.length() / 8; i++) {
            int l = 8 * i, r = Math.min(8 * (i + 1), compressed.length());
            String sub = compressed.substring(l, r);
            bytes.add((byte) Integer.parseInt(sub, 2));
            System.out.print(sub);
        }
        return bytes;
    }

    public String decompress(List<Byte> bytes) {
        int nChars = (int) bytes.get(0);
        HashMap<Character, Integer> freq = new HashMap<>();

        int totalChars = 0;
        for (int i = 0; i < nChars; i++) {
            Character c = (char) ((int) bytes.get(2 * i + 1));
            int f = (int) bytes.get(2 * i + 2);
            freq.put(c, f);

            totalChars += f;
        }

        // build huffman tree
        Node root = buildTree(freq);

        // Get the code of each character
        String[] st = new String[256];
        buildCode(st, root, "");

        HashMap<String, Character> lookupTable = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            String code = st[i];
            if (code == null) continue;
            lookupTable.put(code, (char) i);
        }

        // Get encoded text
        StringBuilder huffmanBuilder = new StringBuilder();
        for (int i = 2*nChars + 1; i < bytes.size(); i++) {
            for (int j = 7; j >= 0; --j) {
                int bit = bytes.get(i) >> j & 1;
                huffmanBuilder.append(bit);
            }
        }

        String huffmanStr = huffmanBuilder.toString();
        System.out.println(huffmanStr);
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < huffmanStr.length() && sb.length() < totalChars; i++) {
            temp.append(huffmanStr.charAt(i));
            Character character = lookupTable.get(temp.toString());
            if (character != null) {
                sb.append(character);
                temp.setLength(0);
            }
        }
        return sb.toString();
    }

}