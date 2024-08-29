import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class OctreeImageQuantization {
    private static BufferedImage originalImage;
    private static BufferedImage quantizedImage;
    private static JFrame frame;
    private static JLabel imageLabel;
    private static OctreeNode root;

    public static void main(String[] args) {
        selectImage();
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
                quantizeImage();
                displayPalette();
                // saveImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No image selected.");
        }
    }

    private static void displayImage(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Image Quantization");
            imageLabel = new JLabel(new ImageIcon(image));
            frame.getContentPane().add(imageLabel, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }

    private static void quantizeImage() {
        int maxColors = 256; // Maximum number of colors in the color palette
        int depth = 5; // Maximum depth of the Octree

        // Build the Octree and quantize the colors
        root = new OctreeNode();
        int totalPixels = originalImage.getWidth() * originalImage.getHeight();
        int colorCount = 0;

        for (int y = 0; y < originalImage.getHeight(); y++) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                Color pixelColor = new Color(originalImage.getRGB(x, y));
                insertColor(root, pixelColor, depth, 2);
                colorCount++;

                if (colorCount * maxColors >= totalPixels) {
                    quantizeColors(root, depth, maxColors);
                    colorCount = 0;
                }
            }
        }

        if (colorCount > 0) {
            quantizeColors(root, depth, maxColors);
        }

        // Create the quantized image
        quantizedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < originalImage.getHeight(); y++) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                Color pixelColor = new Color(originalImage.getRGB(x, y));
                Color quantizedColor = getQuantizedColor(pixelColor, depth);
                quantizedImage.setRGB(x, y, quantizedColor.getRGB());
            }
        }
    }

    private static void insertColor(OctreeNode node, Color color, int remainingDepth, int currentDepth) {
        if (remainingDepth == 0) {
            node.redSum += color.getRed();
            node.greenSum += color.getGreen();
            node.blueSum += color.getBlue();
            node.pixelCount++;
        } else {
            int index = getOctreeChildIndex(color, currentDepth);
            if (node.children[index] == null) {
                node.children[index] = new OctreeNode();
            }
            insertColor(node.children[index], color, remainingDepth - 1, currentDepth + 1);
        }
    }

    private static int getOctreeChildIndex(Color color, int depth) {
        int index = 0;
        int shift = 7 - depth;
        if (color.getRed() >= (1 << shift))
            index |= 4;
        if (color.getGreen() >= (1 << shift))
            index |= 2;
        if (color.getBlue() >= (1 << shift))
            index |= 1;
        return index;
    }

    private static void quantizeColors(OctreeNode node, int remainingDepth, int maxColors) {
        if (node.pixelCount > maxColors) {
            int redSum = 0;
            int greenSum = 0;
            int blueSum = 0;
            int pixelCount = 0;

            for (int i = 0; i < 8; i++) {
                if (node.children[i] != null) {
                    redSum += node.children[i].redSum;
                    greenSum += node.children[i].greenSum;
                    blueSum += node.children[i].blueSum;
                    pixelCount += node.children[i].pixelCount;
                    node.children[i] = null;
                }
            }

            if (pixelCount > 0) {
                node.redSum = redSum;
                node.greenSum = greenSum;
                node.blueSum = blueSum;
                node.pixelCount = pixelCount;
            }
        }

        if (remainingDepth > 0) {
            for (int i = 0; i < 8; i++) {
                if (node.children[i] != null) {
                    quantizeColors(node.children[i], remainingDepth - 1, maxColors);
                }
            }
        }
    }

    private static Color getQuantizedColor(Color color, int depth) {
        OctreeNode node = root;

        for (int i = depth; i > 0; i--) {
            int index = getOctreeChildIndex(color, i);
            if (node.children[index] == null)
                break;
            node = node.children[index];
        }

        if (node.pixelCount == 0) {
            return Color.BLACK; // Default color when there are no pixels in the node
        } else {
            int red = node.redSum / node.pixelCount;
            int green = node.greenSum / node.pixelCount;
            int blue = node.blueSum / node.pixelCount;
            return new Color(red, green, blue);
        }
    }

    private static void displayPalette() {
        SwingUtilities.invokeLater(() -> {
            imageLabel.setIcon(new ImageIcon(quantizedImage));
            frame.pack();
        });
    }
}

class OctreeNode {
    int redSum;
    int greenSum;
    int blueSum;
    int pixelCount;
    OctreeNode[] children;

    public OctreeNode() {
        redSum = 0;
        greenSum = 0;
        blueSum = 0;
        pixelCount = 0;
        children = new OctreeNode[8];
    }
}
