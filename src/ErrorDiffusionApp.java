import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ErrorDiffusionApp {

    private static BufferedImage originalImage;
    private static BufferedImage quantizedImage;
    private static BufferedImage errorDiffusedImage;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ErrorDiffusionApp::selectImage);
    }

    private static void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif"));
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(selectedFile);
                displayImage(originalImage);

                SwingWorker<Void, Void> quantizationWorker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        quantizeImage();
                        return null;
                    }

                    @Override
                    protected void done() {
                        displayQuantizedImage();
                        errorDiffuseImage();
                        displayErrorDiffusedImage();
                        saveImage();
                    }
                };
                quantizationWorker.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No image selected.");
        }
    }

    private static void displayImage(BufferedImage image) {
        JFrame frame = new JFrame("Original Image");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private static void quantizeImage() {
        quantizedImage = ErrorDiffusion.errorDiffuse(originalImage);
    }

    private static void displayQuantizedImage() {
        JFrame frame = new JFrame("Quantized Image");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(quantizedImage)), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private static void errorDiffuseImage() {
        errorDiffusedImage = ErrorDiffusion.errorDiffuse(quantizedImage);
    }

    private static void displayErrorDiffusedImage() {
        JFrame frame = new JFrame("Error Diffused Image");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(errorDiffusedImage)), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private static void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Error Diffused Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        int result = fileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();

            try {
                ImageIO.write(errorDiffusedImage, "png", new File(filePath));
                System.out.println("Error diffused image saved successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Image not saved.");
        }
    }
}
