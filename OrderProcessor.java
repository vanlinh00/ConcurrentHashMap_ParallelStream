import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderProcessor {

    // Simple data models
    record Order(int orderId, String vendorId) {}
    record Vendor(String vendorId, String name) {}

    public static void main(String[] args) {
        // 1. Create 1,000 sample orders (from only 5 different vendors)
        List<Order> orders = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            String vendorId = "VENDOR_" + (i % 5);
            orders.add(new Order(i, vendorId));
        }

        // 2. Process all orders using the temporary cache
        processOrders(orders);
    }

    public static void processOrders(List<Order> orders) {
        // Create a temporary map just for this batch of orders
        Map<String, Vendor> vendorCache = new ConcurrentHashMap<>();

        // Process orders in parallel across multiple threads
        orders.parallelStream().forEach(order -> {
            // Get the vendor (from the database the first time, then from the cache)
            Vendor vendor = loadVendor(order.vendorId(), vendorCache);

            // Process the order with the vendor info
            System.out.println("Processed Order #" + order.orderId() + " for " + vendor.name());
        });

        // When this method finishes, 'vendorCache' is automatically thrown away!
    }

    private static Vendor loadVendor(String vendorId, Map<String, Vendor> cache) {
        return cache.computeIfAbsent(vendorId, id -> {
            // This slow code only runs ONCE per unique vendor!
            return fetchVendorFromDatabase(id);
        });
    }

    // Simulates a slow database call
    private static Vendor fetchVendorFromDatabase(String vendorId) {
        System.out.println("---> Calling Database for: " + vendorId);
        try {
            Thread.sleep(500); // Pretend the database takes 0.5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new Vendor(vendorId, "Company " + vendorId);
    }
}