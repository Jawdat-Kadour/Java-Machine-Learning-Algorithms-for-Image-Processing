import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class SelfOrganizingMap {
    private final int numberOfNeurons;
    private int[][] neuronColors;

    public SelfOrganizingMap(int numberOfNeurons) {
        this.numberOfNeurons = numberOfNeurons;
        this.neuronColors = new int[numberOfNeurons][3];
    }

    public void train(int[][] pixels) {
        int numberOfPixels = pixels.length;
        int numberOfFeatures = pixels[0].length;

        // Initialize neuron weights randomly
        Random random = new Random();
        int[][] neuronWeightsMatrix = new int[numberOfNeurons][numberOfFeatures];
        for (int i = 0; i < numberOfNeurons; i++) {
            for (int j = 0; j < numberOfFeatures; j++) {
                neuronWeightsMatrix[i][j] = random.nextInt(256);
            }
        }

        // SOM training parameters
        double learningRate = 0.9;
        int numberOfIterations = 500;

        // SOM training algorithm
        for (int iteration = 0; iteration < numberOfIterations; iteration++) {
            // Randomly shuffle the pixels
            shufflePixels(pixels);

            // Update neuron weights for each pixel
            for (int i = 0; i < numberOfPixels; i++) {
                int[] pixel = pixels[i];
                int nearestNeuron = findNearestNeuron(pixel, neuronWeightsMatrix);
                updateNeuronWeights(pixel, neuronWeightsMatrix, nearestNeuron, learningRate);
            }

            // Decrease the learning rate
            learningRate *= 0.1;
        }

        // Set the neuron weights as representative colors
        neuronColors = neuronWeightsMatrix;
    }

    private void shufflePixels(int[][] pixels) {
        Random random = new Random();
        for (int i = pixels.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = pixels[i];
            pixels[i] = pixels[j];
            pixels[j] = temp;
        }
    }

    private int findNearestNeuron(int[] pixel, int[][] neuronWeightsMatrix) {
        int nearestNeuron = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < numberOfNeurons; i++) {
            double distance = euclideanDistance(pixel, neuronWeightsMatrix[i]);
            if (distance < minDistance) {
                minDistance = distance;
                nearestNeuron = i;
            }
        }

        return nearestNeuron;
    }

    private void updateNeuronWeights(int[] pixel, int[][] neuronWeightsMatrix, int nearestNeuron, double learningRate) {
        int[] neuronWeights = neuronWeightsMatrix[nearestNeuron];
        for (int i = 0; i < neuronWeights.length; i++) {
            neuronWeights[i] += (int) (learningRate * (pixel[i] - neuronWeights[i]));
        }
    }

    private double euclideanDistance(int[] a, int[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public int[] assignPixels(int[][] pixels) {
        int numberOfPixels = pixels.length;
        int[] assignedNeurons = new int[numberOfPixels];

        for (int i = 0; i < numberOfPixels; i++) {
            int[] pixel = pixels[i];
            assignedNeurons[i] = findNearestNeuron(pixel, neuronColors);
        }

        return assignedNeurons;
    }

    public void displayNeuronColors() {
        int width = numberOfNeurons * 20;
        int height = 50;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < numberOfNeurons; i++) {
            int[] color = neuronColors[i];
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < 20; k++) {
                    image.setRGB(i * 20 + j, k, (color[0] << 16) | (color[1] << 8) | color[2]);
                }
            }
        }

        JFrame frame = new JFrame("SOM Neuron Colors");
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        // Load image
        FileDialog dialog = new FileDialog((Frame) null, "Select Image to Train SOM");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        String filename = dialog.getFile();
        if (filename == null) {
            System.exit(0);
        }
        String imagePath = dialog.getDirectory() + File.separator + filename;
        BufferedImage image = loadImage(imagePath);

        // Convert image pixels to 2D array
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] pixels = new int[width * height][3];
        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = image.getRGB(j, i);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                pixels[index][0] = r;
                pixels[index][1] = g;
                pixels[index][2] = b;
                index++;
            }
        }

        // Train SOM
        int numberOfNeurons = 32;
        SelfOrganizingMap som = new SelfOrganizingMap(numberOfNeurons);
        som.train(pixels);
        // Display the quantized image
        som.displayQuantizedImage(pixels, width, height);

        // Display SOM neuron colors
        som.displayNeuronColors();
    }

    public void displayQuantizedImage(int[][] pixels, int width, int height) {
        // Create a new BufferedImage from the quantized pixels
        BufferedImage quantizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixelIndex = i * width + j;
                int[] color = neuronColors[assignPixels(new int[][] { pixels[pixelIndex] })[0]];
                int rgb = (color[0] << 16) | (color[1] << 8) | color[2];
                quantizedImage.setRGB(j, i, rgb);
            }
        }

        // Display the quantized image in a new window
        JFrame frame = new JFrame("Quantized Image");
        JLabel label = new JLabel(new ImageIcon(quantizedImage));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private static BufferedImage loadImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        return ImageIO.read(imageFile);
    }
}