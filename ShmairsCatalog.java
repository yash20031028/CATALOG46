import org.json.JSONObject;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class ShamirSecretSharing {

    // Helper method to decode a number in a given base to base 10 (decimal)
    public static long decodeY(String yValue, int base) {
        return Long.parseLong(yValue, base); // Long for large values
    }

    // Function to find the constant term 'c' of the polynomial using Lagrange interpolation
    public static double findConstantTerm(double[] x, double[] y) {
        int k = x.length;
        double constantTerm = 0.0;

        for (int i = 0; i < k; i++) {
            double li = 1.0; // Lagrange basis polynomial L_i(0)

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    li *= (0 - x[j]) / (x[i] - x[j]);
                }
            }

            constantTerm += y[i] * li;
        }

        return constantTerm;
    }

    // Function to identify wrong points (imposter points)
    public static List<Integer> findWrongPoints(TreeMap<Integer, Double> pointsMap, double secret) {
        List<Integer> wrongPoints = new ArrayList<>();
        int n = pointsMap.size();
        int k = 6; // Assuming k is 6

        for (int i = 0; i < n - k + 1; i++) {
            double[] x = new double[k];
            double[] y = new double[k];
            int index = 0;

            for (Map.Entry<Integer, Double> entry : pointsMap.entrySet()) {
                if (index >= k) break;
                x[index] = entry.getKey();
                y[index] = entry.getValue();
                index++;
            }

            double reconstructedSecret = findConstantTerm(x, y);

            if (reconstructedSecret != secret) {
                wrongPoints.add(x[0]); // Assuming the first point in the subset is the wrong one
            }
        }

        return wrongPoints;
    }

    public static void main(String[] args) {
        try {
            // Read the JSON file
            FileReader reader = new FileReader("testcase.json"); // Replace with your file path
            StringBuilder jsonString = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                jsonString.append((char) i);
            }
            reader.close();

            // Parse the JSON string
            JSONObject jsonObject = new JSONObject(jsonString.toString());

            // Extract the keys for the number of points 'n' and required points 'k'
            JSONObject keysObject = jsonObject.getJSONObject("keys");
            int n = keysObject.getInt("n");
            int k = keysObject.getInt("k");

            // Map to store the x and decoded y values (we use TreeMap to keep the keys sorted)
            TreeMap<Integer, Double> pointsMap = new TreeMap<>();

            // Iterate through the rest of the JSON object to get the points
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();

                // Skip the "keys" section
                if (key.equals("keys")) continue;

                // Get the base and value for the current point
                JSONObject pointObject = jsonObject.getJSONObject(key);
                String base = pointObject.getString("base");
                String value = pointObject.getString("value");

                // Decode the y value using the given base
                long decodedY = decodeY(value, Integer.parseInt(base));

                // Store the x (as the key from the JSON) and the decoded y value
                pointsMap.put(Integer.parseInt(key), (double) decodedY);
            }

            // Now, we need to use the first 'k' points for the interpolation
            double[] x = new double[k];
            double[] y = new double[k];
            int index = 0;
            for (Map.Entry<Integer, Double> entry : pointsMap.entrySet()) {
                if (index >= k) break;
                x[index] = entry.getKey();  // x-values (keys)
                y[index] = entry.getValue();  // y-values (decoded values)
                index++;
            }

            // Find the constant term 'c' using Lagrange interpolation
            double secret = findConstantTerm(x, y);

            // Identify wrong points
            List<Integer> wrongPoints = findWrongPoints(pointsMap, secret);

            System.out.println("The secret (constant term 'c') is: " + secret);
            System.out.println("Wrong points: " + wrongPoints);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}