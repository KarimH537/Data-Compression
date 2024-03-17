import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        while (true) {
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            String file = "";
            if (choice == 1) {
                file = "input.txt";
            } else if (choice == 2) {
                file = "output.txt";
            } else {
                return;
            }
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String result, output, tmp;
                StringBuilder line = new StringBuilder();
                while ((tmp = reader.readLine()) != null) {
                    line.append(tmp).append('\n');
                }
                reader.close();
                CompressionStrategy cmp = new LZ77();
                if (choice == 1) {
                    result = cmp.compress(line.toString());
                    output = "output.txt";
                } else {
                    result = cmp.decompress(line.toString());
                    output = "input.txt";
                }
                try {
                    FileWriter myWriter = new FileWriter(output);
                    myWriter.write(result);
                    myWriter.close();
                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}

