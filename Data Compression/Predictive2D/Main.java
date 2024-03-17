import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class Gui {
    private final JFrame frame;
    private final JTextField textField;
    private final JLabel statusLabel;

    public Gui() {
        frame = new JFrame("2D prediction");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(4, 1));

        JLabel titleLabel = new JLabel("2D prediction", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        frame.getContentPane().add(titleLabel);

        JPanel filePanel = new JPanel();
        filePanel.add(new JLabel("File path:"));
        textField = new JTextField(20);
        textField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        filePanel.add(textField);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(frame);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                textField.setText(path);
            }
        });

        filePanel.add(browseButton);
        frame.getContentPane().add(filePanel);

        JPanel buttonPanel = new JPanel();
        JButton compressButton = new JButton("Compress");
        JButton decompressButton = new JButton("Decompress");
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);
        frame.getContentPane().add(buttonPanel);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        frame.getContentPane().add(statusLabel);
        compressButton.addActionListener(e -> {
            String filePath = textField.getText();
            compressFile(filePath);
        });

        decompressButton.addActionListener(e -> {
            String filePath = textField.getText();
            decompressFile(filePath);
        });

        frame.setVisible(true);
    }

    private List<Byte> readByteFile(String file) throws IOException {
        File f = new File(file);
        byte[] fileBytes = Files.readAllBytes(f.toPath());
        List<Byte> bytes = new ArrayList<>();
        for (byte b : fileBytes) {
            bytes.add(b);
        }
        return bytes;
    }

    private void saveByteFile(String file, List<Byte> content) throws IOException {
        FileOutputStream myWriter = new FileOutputStream(file);
        for (Byte b : content) {
            myWriter.write(b);
        }
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
            int[][] input = ImageRW.readImage(file);

            Quantizer cmp = new Predictive2D();
            List<Byte> result = cmp.compress(input);
            saveByteFile("compressed.pred", result);
            showSuccess();
        } catch (Exception e) {
            showError();
        }
    }

    private void decompressFile(String file) {
        try {
            List<Byte> input = readByteFile(file);
            Quantizer cmp = new Predictive2D();
            int[][] result = cmp.decompress(input);
            ImageRW.writeImage(result, result[0].length, result.length, "decompressed.jpg");
            showSuccess();
        } catch (Exception e) {
            showError();
        }
    }
}

public class Main {

    public static void main(String[] args) {
        new Gui();
    }
}