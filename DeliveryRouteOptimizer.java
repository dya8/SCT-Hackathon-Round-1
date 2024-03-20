import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Location {
    String order_id;
    double lat;
    double lng;

    public Location(String order_id, double lat, double lng) {
        this.order_id = order_id;
        this.lat = lat;
        this.lng = lng;
    }
}

public class DeliveryRouteOptimizer {
    static final double RADIUS_OF_EARTH = 6371.0; // Earth radius in kilometers

    public static void main(String[] args) {
        String filename = "part_a_input_dataset_1.csv"; // Change to appropriate filename
        List<Location> locations = readDataset(filename);

        if (locations.size() > 0) {
            Location depot = locations.get(0);
            List<Location> customerLocations = locations.subList(1, locations.size());

            // Calculate distances
            Map<String, Double> distances = calculateDistances(depot, customerLocations);

            // Find best delivery route
            List<String> bestRoute = findBestRoute(depot, customerLocations, distances);

            // Output best delivery route
            System.out.println("Best delivery route:");
            for (String location : bestRoute) {
                System.out.println(location);
            }
        } else {
            System.out.println("No data found in the input file.");
        }
    }

    public static List<Location> readDataset(String filename) {
        List<Location> locations = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header line
                }
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String order_id = parts[0];
                    double lat = Double.parseDouble(parts[1]);
                    double lng = Double.parseDouble(parts[2]);
                    Location location = new Location(order_id, lat, lng);
                    locations.add(location);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;
    }

  
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double delta_phi = Math.toRadians(lat2 - lat1);
        double delta_lambda = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(delta_phi / 2), 2) + Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(delta_lambda / 2), 2);
        double res = RADIUS_OF_EARTH  * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        return Math.round(res * 100.0) / 100.0; // Rounding to two decimal places
    }


    public static Map<String, Double> calculateDistances(Location depot, List<Location> locations) {
        Map<String, Double> distances = new HashMap<>();
        for (Location location : locations) {
            double distance = haversine(depot.lat, depot.lng, location.lat, location.lng);
            distances.put(location.order_id, distance);
        }
        return distances;
    }

    public static List<String> findBestRoute(Location depot, List<Location> locations, Map<String, Double> distances) {
        List<String> bestRoute = new ArrayList<>();
        double minDistance = Double.MAX_VALUE;
        List<Location> permutation = new ArrayList<>();
        permutation.add(depot);
        for (Location location : locations) {
            permutation.add(location);
        }
        permute(permutation, 1, distances, new ArrayList<>(), minDistance, bestRoute);
        return bestRoute;
    }

    public static void permute(List<Location> permutation, int index, Map<String, Double> distances,
                                List<String> currentRoute, double minDistance, List<String> bestRoute) {
        if (index == permutation.size()) {
            double totalDistance = calculateTotalDistance(permutation, distances);
            if (totalDistance < minDistance) {
                minDistance = totalDistance;
                bestRoute.clear();
                for (String location : currentRoute) {
                    bestRoute.add(location);
                }
            }
        } else {
            for (int i = index; i < permutation.size(); i++) {
                swap(permutation, index, i);
                currentRoute.add(permutation.get(index).order_id);
                permute(permutation, index + 1, distances, currentRoute, minDistance, bestRoute);
                currentRoute.remove(currentRoute.size() - 1);
                swap(permutation, index, i);
            }
        }
    }

    public static void swap(List<Location> permutation, int i, int j) {
        Location temp = permutation.get(i);
        permutation.set(i, permutation.get(j));
        permutation.set(j, temp);
    }

    public static double calculateTotalDistance(List<Location> permutation, Map<String, Double> distances) {
        double totalDistance = 0;
        for (int i = 1; i < permutation.size(); i++) {
            String currentLocation = permutation.get(i).order_id;
            String prevLocation = permutation.get(i - 1).order_id;
            totalDistance += distances.get(prevLocation + "-" + currentLocation);
        }
        return totalDistance;
    }
}

