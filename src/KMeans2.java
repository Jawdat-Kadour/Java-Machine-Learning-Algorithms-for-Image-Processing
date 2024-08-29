
import java.util.Arrays;
import java.util.Random;

public class KMeans2 {
    public static double[][] kmeans(double[][] data, int numClusters) {
        int numSamples = data.length;
        int numFeatures = data[0].length;

        // Initialize centroids randomly
        Random random = new Random();
        double[][] centroids = new double[numClusters][numFeatures];
        for (int i = 0; i < numClusters; i++) {
            int randomIndex = random.nextInt(numSamples);
            centroids[i] = Arrays.copyOf(data[randomIndex], numFeatures);
        }

        // Initialize cluster assignments
        int[] clusterAssignments = new int[numSamples];

        boolean converged = false;
        while (!converged) {
            // Assign each sample to the nearest centroid
            boolean assignmentsChanged = updateClusterAssignments(data, centroids, clusterAssignments);

            if (!assignmentsChanged) {
                converged = true;
            } else {
                // Update centroids
                updateCentroids(data, numClusters, numFeatures, centroids, clusterAssignments);
            }
        }

        return centroids;
    }

    private static boolean updateClusterAssignments(double[][] data, double[][] centroids, int[] clusterAssignments) {
        boolean assignmentsChanged = false;
        int numSamples = data.length;
        int numClusters = centroids.length;

        for (int i = 0; i < numSamples; i++) {
            double[] sample = data[i];
            int nearestCluster = assignToNearestCluster(sample, centroids);
            if (clusterAssignments[i] != nearestCluster) {
                clusterAssignments[i] = nearestCluster;
                assignmentsChanged = true;
            }
        }

        return assignmentsChanged;
    }

    private static void updateCentroids(double[][] data, int numClusters, int numFeatures, double[][] centroids,
            int[] clusterAssignments) {
        int numSamples = data.length;

        // Initialize sums and counts for each cluster
        double[][] sums = new double[numClusters][numFeatures];
        int[] counts = new int[numClusters];

        // Calculate sums of features and counts for each cluster
        for (int i = 0; i < numSamples; i++) {
            int cluster = clusterAssignments[i];
            double[] sample = data[i];
            double[] sum = sums[cluster];
            int count = counts[cluster];

            for (int feature = 0; feature < numFeatures; feature++) {
                sum[feature] += sample[feature];
            }

            counts[cluster]++;
        }

        // Update centroids using the calculated sums and counts
        for (int cluster = 0; cluster < numClusters; cluster++) {
            double[] centroid = centroids[cluster];
            double[] sum = sums[cluster];
            int count = counts[cluster];

            if (count > 0) {
                for (int feature = 0; feature < numFeatures; feature++) {
                    centroid[feature] = sum[feature] / count;
                }
            }
        }
    }

    private static int assignToNearestCluster(double[] sample, double[][] centroids) {
        int numClusters = centroids.length;
        int nearestCluster = 0;
        double minDistance = Double.MAX_VALUE;

        for (int cluster = 0; cluster < numClusters; cluster++) {
            double[] centroid = centroids[cluster];
            double distance = euclideanDistance(sample, centroid);
            if (distance < minDistance) {
                minDistance = distance;
                nearestCluster = cluster;
            }
        }

        return nearestCluster;
    }

    private static double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        int length = a.length;
        for (int i = 0; i < length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public static void main(String[] args) {
        // Example usage
        double[][] data = {
                { 1.0, 2.0 },
                { 2.0, 1.5 },
                { 10.0, 12.0 },
                { 12.0, 10.0 }
        };

        int numClusters = 2;
        double[][] centroids = kmeans(data, numClusters);

        System.out.println("Centroids:");
        for (double[] centroid : centroids) {
            System.out.println(Arrays.toString(centroid));
        }
    }
}
