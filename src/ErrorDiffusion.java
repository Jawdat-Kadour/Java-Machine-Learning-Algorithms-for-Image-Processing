import java.awt.Color;
import java.awt.image.BufferedImage;

public class ErrorDiffusion {

    private static final int[] dx = { 1, -1, 0, 1 };
    private static final int[] dy = { 0, 1, 1, 1 };
    private static final int[][] diffusionMatrix = { { 0, 0, 7 }, { 3, 5, 1 } };

    public static BufferedImage errorDiffuse(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        int[][] pixels = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int) (0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF) + 0.114 * (rgb & 0xFF));
                pixels[y][x] = gray;
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int oldPixel = pixels[y][x];
                int newPixel = oldPixel < 128 ? 0 : 255;
                output.setRGB(x, y, new Color(newPixel, newPixel, newPixel).getRGB());
                int quantError = oldPixel - newPixel;
                for (int i = 0; i < dx.length; i++) {
                    int nx = x + dx[i];
                    int ny = y + dy[i];
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                        pixels[ny][nx] += quantError * diffusionMatrix[i / 2][i % 2] / 16;
                    }
                }
            }
        }
        return output;
    }
}
