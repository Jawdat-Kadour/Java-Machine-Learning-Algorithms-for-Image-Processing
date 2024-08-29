import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class ImageQuantization {
    public static void main(String[] args) {
        // Load an image using a GUI
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "bmp", "tif"));

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            quantizeColors(selectedFile);

        }
    }

    public static int[][] quantizeColors(File selectedFile) {
        int[][] quantizedImage = null;
        try {
            // Read the image file
            BufferedImage img = ImageIO.read(selectedFile);

            // Display the original image
            displayImage(img, "Original Image");

            // Convert the image to RGB color space
            img = toRGB(img);

            // Define the number of colors to quantize
            int numColors = 26;

            // Method 1: Median cut algorithm
            // Reshape the image into a matrix of pixels by colors
            double[][] pixels = reshapeImage(img);

            // Initialize an array of color bins
            double[][][] bins = new double[1][pixels.length][3];
            bins[0] = pixels;

            // Loop until the number of bins is equal to the number of colors
            while (bins.length < numColors) {
                // Find the bin with the largest range in any color dimension
                double maxRange = 0;
                int maxBin = 0;
                for (int i = 0; i < bins.length; i++) {
                    double[] binRange = computeRange(bins[i]);
                    if (hasRangeGreaterThan(binRange, maxRange)) {
                        maxRange = getMaxValue(binRange);
                        maxBin = i;
                    }
                }

                // Split the bin along the dimension with the largest range
                double[][] bin = bins[maxBin];
                bins = removeBinFromBins(bins, maxBin);
                double medianValue = computeMedian(bin, getDimensionWithMaxRange(bin));
                double[][] bin1 = filterBin(bin, getDimensionWithMaxRange(bin), "<=",
                        medianValue);
                double[][] bin2 = filterBin(bin, getDimensionWithMaxRange(bin), ">",
                        medianValue);
                // Add the new bins to the array
                bins = addBinToBins(bins, bin1);
                bins = addBinToBins(bins, bin2);
            }

            // Compute the representative color for each bin as the mean color
            double[][] colors = new double[numColors][3];
            for (int i = 0; i < numColors; i++) {
                colors[i] = computeMean(bins[i]);
            }
            // Assign each pixel to the nearest color using Manhanten distance
            int[][] indices = new int[pixels.length][numColors];
            for (int i = 0; i < pixels.length; i++) {
                indices[i] = assignPixelToNearestColor(pixels[i], colors);
            }

            // Reshape the indices into an indexed image
            quantizedImage = reshapeIndices(indices, img.getWidth(), img.getHeight());

            // Display the quantized image using the colormap
            displayImage(quantizedImage, colors, "Quantized Image using Median Cut Algorithm");
            displayColorPalette(colors);
            displayHistogram(quantizedImage);

            // Method 2: K-means clustering algorithm
            // Reshape the image into a matrix of pixels by colors
            double[][] pixels2 = reshapeImage(img);

            // Use kmeans function to cluster the pixels into numColors groups
            double[][] centroids = KMeans2.kmeans(pixels2, numColors);

            // Reshape the indices into an indexed image
            int[][] quantizedImage2 = reshapeIndices(indices, img.getWidth(),
                    img.getHeight());

            // Display the quantized image using the colormap
            displayImage(quantizedImage2, centroids, "Quantized Image using K-means Clustering");
            displayColorPalette(centroids);
            displayHistogram(quantizedImage2);
            // Generate the file name with a date stamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outputFileName = "quantized_image_" + timeStamp + ".png";

            // Choose the save path using a GUI
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setDialogTitle("Save Quantized Image");
            saveFileChooser.setSelectedFile(new File(outputFileName));
            int saveReturnValue = saveFileChooser.showSaveDialog(null);

            if (saveReturnValue == JFileChooser.APPROVE_OPTION) {
                File outputImageFile = saveFileChooser.getSelectedFile();

                // Reshape the quantized image into a BufferedImage
                BufferedImage bufferedImage = new BufferedImage(quantizedImage[0].length,
                        quantizedImage.length,
                        BufferedImage.TYPE_INT_RGB);
                BufferedImage bufferedImage2 = new BufferedImage(quantizedImage2[0].length,
                        quantizedImage2.length,
                        BufferedImage.TYPE_INT_RGB);
                int width2 = quantizedImage2[0].length;
                int height2 = quantizedImage2.length;
                int width = quantizedImage[0].length;
                int height = quantizedImage.length;
                // BufferedImage bu = new BufferedImage(width, height,
                // BufferedImage.TYPE_INT_RGB);
                Color[] colorMap = createColorMap(colors);
                Color[] colorMap2 = createColorMap(centroids);

                for (int y = 0; y < height2; y++) {
                    for (int x = 0; x < width2; x++) {
                        int colorIndex = quantizedImage2[y][x];
                        bufferedImage2.setRGB(x, y, colorMap2[colorIndex].getRGB());
                    }
                }
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int colorIndex = quantizedImage[y][x];
                        bufferedImage.setRGB(x, y, colorMap[colorIndex].getRGB());
                    }
                }

                // Save the quantized image to the specified file location

                // displayImage(bufferedImage2, outputFileName);

                // Save the quantized image to the specified file location
                ImageIO.write(bufferedImage, "png", outputImageFile);
                ImageIO.write(bufferedImage2, "png", outputImageFile);

            }

        } catch (

        IOException e) {
            e.printStackTrace();
        }
        return quantizedImage;

    }

    // Helper method to display the color palette
    private static void displayColorPalette(double[][] colors) {
        int paletteWidth = 50;
        int paletteHeight = 20;
        int numColors = colors.length;
        int width = paletteWidth * numColors;
        BufferedImage paletteImage = new BufferedImage(width, paletteHeight, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < numColors; i++) {
            int startX = i * paletteWidth;
            int endX = startX + paletteWidth;
            Color color = new Color((int) colors[i][0], (int) colors[i][1], (int) colors[i][2]);

            for (int x = startX; x < endX; x++) {
                for (int y = 0; y < paletteHeight; y++) {
                    paletteImage.setRGB(x, y, color.getRGB());
                }
            }
        }

        displayImage(paletteImage, "Color Palette");
    }

    private static void displayHistogram(int[][] quantizedImage) {
        int width = quantizedImage[0].length;
        int height = quantizedImage.length;
        int[] histogram = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int colorIndex = quantizedImage[y][x];
                histogram[colorIndex]++;
            }
        }

        int maxFrequency = Arrays.stream(histogram).max().orElse(0);
        int histogramHeight = 150;
        int histogramWidth = histogram.length;
        int barWidth = (int) Math.ceil((double) histogramWidth / width);

        BufferedImage histogramImage = new BufferedImage(histogramWidth, histogramHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = histogramImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, histogramWidth, histogramHeight);

        g2d.setColor(Color.BLACK);
        for (int i = 0; i < histogram.length; i++) {
            int columnHeight = histogram[i] * histogramHeight / maxFrequency;
            int startX = i * barWidth;
            int startY = histogramHeight - columnHeight;
            int barHeight = columnHeight;

            g2d.fillRect(startX, startY, barWidth, barHeight);
        }

        g2d.dispose();

        displayImage(histogramImage, "Histogram");
    }

    // Helper method to convert an image to RGB color space
    private static BufferedImage toRGB(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = convertedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return convertedImage;
    }

    // Helper method to reshape the image into a matrix of pixels by colors
    private static double[][] reshapeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] pixels = new double[width * height][3];
        int pixelIndex = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                pixels[pixelIndex][0] = color.getRed();
                pixels[pixelIndex][1] = color.getGreen();
                pixels[pixelIndex][2] = color.getBlue();
                pixelIndex++;
            }
        }

        return pixels;
    }

    // Helper method to compute the range of each color dimension in a bin
    private static double[] computeRange(double[][] bin) {
        int numDimensions = bin[0].length;
        double[] range = new double[numDimensions];
        Arrays.fill(range, Double.MIN_VALUE);

        for (int i = 0; i < bin.length; i++) {
            for (int j = 0; j < numDimensions; j++) {
                range[j] = Math.max(range[j], bin[i][j]);
            }
        }

        return range;
    }

    // Helper method to check if any value in the range array is greater than a
    // threshold
    private static boolean hasRangeGreaterThan(double[] range, double threshold) {
        for (double value : range) {
            if (value > threshold) {
                return true;
            }
        }
        return false;
    }

    // Helper method to get the maximum value from an array
    private static double getMaxValue(double[] array) {
        double max = Double.MIN_VALUE;
        for (double value : array) {
            max = Math.max(max, value);
        }
        return max;
    }

    // Helper method to compute the dimension with the maximum range in a bin
    private static int getDimensionWithMaxRange(double[][] bin) {
        double[] range = computeRange(bin);
        int maxDimension = 0;
        double maxRange = Double.MIN_VALUE;

        for (int i = 0; i < range.length; i++) {
            if (range[i] > maxRange) {
                maxDimension = i;
                maxRange = range[i];
            }
        }

        return maxDimension;
    }

    // Helper method to compute the median value of a dimension in a bin
    private static double computeMedian(double[][] bin, int dimension) {
        double[] values = new double[bin.length];
        for (int i = 0; i < bin.length; i++) {
            values[i] = bin[i][dimension];
        }
        Arrays.sort(values);
        return values[values.length / 2];
    }

    // Helper method to filter a bin based on a comparison operator and a threshold
    // value
    private static double[][] filterBin(double[][] bin, int dimension, String operator, double threshold) {
        double[][] filteredBin = new double[bin.length][bin[0].length];
        int filteredIndex = 0;

        for (int i = 0; i < bin.length; i++) {
            double value = bin[i][dimension];
            boolean keep = false;

            switch (operator) {
                case "<":
                    keep = value < threshold;
                    break;
                case "<=":
                    keep = value <= threshold;
                    break;
                case ">":
                    keep = value > threshold;
                    break;
                case ">=":
                    keep = value >= threshold;
                    break;
            }

            if (keep) {
                filteredBin[filteredIndex] = bin[i];
                filteredIndex++;
            }
        }

        return Arrays.copyOf(filteredBin, filteredIndex);
    }

    // Helper method to remove a bin from an array of bins
    private static double[][][] removeBinFromBins(double[][][] bins, int index) {
        double[][][] newBins = new double[bins.length - 1][][];
        System.arraycopy(bins, 0, newBins, 0, index);
        System.arraycopy(bins, index + 1, newBins, index, bins.length - index - 1);
        return newBins;
    }

    // Helper method to add a bin to an array of bins
    private static double[][][] addBinToBins(double[][][] bins, double[][] bin) {
        double[][][] newBins = new double[bins.length + 1][][];
        System.arraycopy(bins, 0, newBins, 0, bins.length);
        newBins[bins.length] = bin;
        return newBins;
    }

    // Helper method to compute the mean color of a bin
    private static double[] computeMean(double[][] bin) {
        double[] mean = new double[bin[0].length];

        for (int i = 0; i < bin.length; i++) {
            for (int j = 0; j < mean.length; j++) {
                mean[j] += bin[i][j];
            }
        }

        for (int j = 0; j < mean.length; j++) {
            mean[j] /= bin.length;
        }

        return mean;
    }

    private static int[] assignPixelToNearestColor(double[] pixel, double[][] colors) {
        double minDistanceSquared = Double.MAX_VALUE;
        int nearestColorIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            // double distanceSquared = computeDistanceSquared(pixel, colors[i]);
            double distanceSquared = computeManhattanDistance(pixel, colors[i]);
            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                nearestColorIndex = i;
            }
        }

        return new int[] { nearestColorIndex };
    }

    // private static double computeDistanceSquared(double[] color1, double[]
    // color2) {
    // double distanceSquared = 0.0;
    // for (int i = 0; i < color1.length; i++) {
    // double diff = color1[i] - color2[i];
    // distanceSquared += diff * diff;
    // }
    // return distanceSquared;
    // }

    private static double computeManhattanDistance(double[] color1, double[] color2) {
        double distance = 0.0;
        for (int i = 0; i < color1.length; i++) {
            distance += Math.abs(color1[i] - color2[i]);
        }
        return distance;
    }

    // Helper method to reshape the indices into an indexed image
    private static int[][] reshapeIndices(int[][] indices, int width, int height) {
        int[][] reshapedIndices = new int[height][width];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                reshapedIndices[y][x] = indices[index][0];
                index++;
            }
        }

        return reshapedIndices;
    }

    // Helper method to display an image using the colormap
    private static void displayImage(int[][] image, double[][] colors, String title) {
        int width = image[0].length;
        int height = image.length;
        BufferedImage quantizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Color[] colorMap = createColorMap(colors);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int colorIndex = image[y][x];
                quantizedImage.setRGB(x, y, colorMap[colorIndex].getRGB());
            }
        }

        displayImage(quantizedImage, title);
    }

    // Helper method to create a color map from an array of colors
    private static Color[] createColorMap(double[][] colors) {
        Color[] colorMap = new Color[colors.length];
        Random random = new Random();

        for (int i = 0; i < colors.length; i++) {
            int r = (int) colors[i][0];
            int g = (int) colors[i][1];
            int b = (int) colors[i][2];

            // Add a small random value to each color component to avoid banding
            r = clamp(r + random.nextInt(3) - 1, 0, 255);
            g = clamp(g + random.nextInt(3) - 1, 0, 255);
            b = clamp(b + random.nextInt(3) - 1, 0, 255);

            colorMap[i] = new Color(r, g, b);
        }

        return colorMap;
    }

    // Helper method to clamp a value between a minimum and maximum
    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    // Helper method to display an image in a GUI window
    private static void displayImage(BufferedImage image, String title) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JFrame frame = new javax.swing.JFrame(title);
            frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().setLayout(new java.awt.BorderLayout());
            frame.getContentPane().add(new javax.swing.JLabel(new javax.swing.ImageIcon(image)),
                    java.awt.BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
