import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class LZWGui {
    private JFrame frame;
    private JTextField textField;
    private JButton compressButton;
    private JButton decompressButton;
    private JLabel statusLabel;

    public LZWGui() {
        frame = new JFrame("LZW");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(4, 1));

        JLabel titleLabel = new JLabel("LZW", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        frame.getContentPane().add(titleLabel);

        JPanel filePanel = new JPanel();
        filePanel.add(new JLabel("File path:"));
        textField = new JTextField(20);
        textField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        filePanel.add(textField);
        frame.getContentPane().add(filePanel);

        JPanel buttonPanel = new JPanel();
        compressButton = new JButton("Compress");
        decompressButton = new JButton("Decompress");
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);
        frame.getContentPane().add(buttonPanel);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        frame.getContentPane().add(statusLabel);

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = textField.getText();
                processFile(filePath, true);
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = textField.getText();
                processFile(filePath, false);
            }
        });

        frame.setVisible(true);
    }

    private void processFile(String file, boolean isCompression) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String result, output, tmp;
            StringBuilder line = new StringBuilder();
            while ((tmp = reader.readLine()) != null) {
                line.append(tmp).append('\n');
            }
            reader.close();
            CompressionStrategy cmp = new LZW();
            if (isCompression) {
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
                statusLabel.setText("Done");
                statusLabel.setForeground(Color.GREEN);
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LZWGui();
    }
}
