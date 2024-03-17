import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

class LZWGui {
    private JFrame frame;
    private JTextField textField;
    private JButton compressButton;
    private JButton decompressButton;
    private JButton browseButton;
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

        browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(frame);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    String path = fileChooser.getSelectedFile().getAbsolutePath();
                    textField.setText(path);
                }
            }
        });

        filePanel.add(browseButton);
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
                compressFile(filePath);
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = textField.getText();
                decompressFile(filePath);
            }
        });

        frame.setVisible(true);
    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String tmp;
        StringBuilder line = new StringBuilder();
        while ((tmp = reader.readLine()) != null) {
            line.append(tmp).append('\n');
        }
        reader.close();
        return line.toString();
    }

    private void saveFile(String file, String content) throws IOException {
        FileWriter myWriter = new FileWriter(file);
        myWriter.write(content);
        myWriter.close();
    }

    private void showSuccess() {
        statusLabel.setText("Done");
        statusLabel.setForeground(Color.GREEN);
    }

    private void showError() {
        statusLabel.setText("An error occurred");
        statusLabel.setForeground(Color.RED);
    }

    private void compressFile(String file) {
        try {
            String input = readFile(file);
            CompressionStrategy cmp = new LZW();
            String result = cmp.compress(input);
            saveFile("output.txt", result);
            showSuccess();
        } catch (Exception e) {
            showError();
        }
    }

    private void decompressFile(String file) {
        try {
            String input = readFile(file);
            CompressionStrategy cmp = new LZW();
            String result = cmp.decompress(input);
            saveFile("input.txt", result);
            showSuccess();
        } catch (Exception e) {
            showError();
        }
    }
}

public class Main {

    public static void main(String[] args) {
        new LZWGui();
    }
}