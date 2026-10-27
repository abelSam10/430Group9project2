import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Interactive test for Person 2: Product stock/waitlist and Receive Shipment.
 * Clients and products are created through the real Warehouse and Inventory.
 *
 * Run interactively:      java Person2Test
 * Run only the checks:    java Person2Test --auto
 */
public class Person2Test {
    private static final int EXIT = 0;
    private static final int ADD_CLIENT = 1;
    private static final int ADD_PRODUCT = 2;
    private static final int PLACE_ORDER = 3;
    private static final int RECEIVE_SHIPMENT = 4;
    private static final int DISPLAY_PRODUCTS = 5;
    private static final int DISPLAY_CLIENTS = 6;
    private static final int DISPLAY_WAITLIST = 7;
    private static final int DISPLAY_ORDERS = 8;
    private static final int RUN_CHECKS = 9;
    private static final int HELP = 10;

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static Warehouse warehouse = Warehouse.instance();
    private static Inventory inventory = Inventory.instance();
    private static int checks;

    // ---------------------------------------------------------------- shared logic

    // What Feature 1 (Process Order, Person 3) will do with one order line:
    // fill from stock, charge only the filled part, waitlist the rest.
    // Returns the quantity filled from stock.
    private static int order(Client client, Product product, int requested) {
        int filled = product.removeFromStock(requested);
        if (filled > 0) {
            Order invoice = new Order(client);
            invoice.addItem(new OrderItem(product, filled, product.getSalePrice()));
            client.addOrder(invoice);
            client.debit(invoice.getTotal());
        }
        if (requested > filled) product.addToWaitlist(client, requested - filled);
        return filled;
    }

    // Simulates Feature 1 for a whole wishlist: one invoice for everything filled
    // from stock, the shortfall of each line waitlisted, then the wishlist cleared.
    // Returns the invoice, or null if nothing could be filled.
    private static Order orderWholeWishlist(Client client) {
        Order invoice = new Order(client);
        List<String> ordered = new ArrayList<String>();
        for (Iterator<WishlistItem> items = client.getWishlist(); items.hasNext(); ) {
            WishlistItem item = items.next();
            Product product = item.getProduct();
            int filled = product.removeFromStock(item.getQuantity());
            if (filled > 0) invoice.addItem(new OrderItem(product, filled, product.getSalePrice()));
            if (item.getQuantity() > filled) product.addToWaitlist(client, item.getQuantity() - filled);
            ordered.add(product.getId());
        }
        for (String productID : ordered) client.removeFromWishlist(productID);
        if (!invoice.getItems().hasNext()) return null;
        client.addOrder(invoice);
        client.debit(invoice.getTotal());
        return invoice;
    }

    // Same algorithm as receiveShipment_P2.java.txt, minus the inventory lookup.
    // Keep the two in step.
    private static ShipmentResult receiveShipment(Product product, int quantityReceived) {
        if (quantityReceived <= 0 || product == null) return null;
        ShipmentResult result = new ShipmentResult(product);
        int remaining = quantityReceived;
        while (remaining > 0 && product.hasWaitlist()) {
            WaitlistItem item = product.getFirstWaitlistItem();
            Client client = item.getClient();
            int filled = Math.min(remaining, item.getQuantity());

            Order order = new Order(client);
            order.addItem(new OrderItem(product, filled, product.getSalePrice()));
            client.addOrder(order);
            client.debit(order.getTotal());
            result.addOrder(order);

            if (filled == item.getQuantity()) {
                product.removeWaitlistItem(item);
            } else {
                item.setQuantity(item.getQuantity() - filled);
            }
            remaining -= filled;
        }
        product.addToStock(remaining);
        result.setQuantityAddedToStock(remaining);
        return result;
    }

    private static String waitlistString(Product product) {
        StringBuilder text = new StringBuilder();
        for (Iterator iterator = product.getWaitlist(); iterator.hasNext(); ) {
            WaitlistItem item = (WaitlistItem) iterator.next();
            if (text.length() > 0) text.append(", ");
            text.append(item.getClient().getName()).append(" x ").append(item.getQuantity());
        }
        return text.toString();
    }

    private static Client findClient(String clientID) {
        for (Iterator<Client> clients = warehouse.getClients(); clients.hasNext(); ) {
            Client client = clients.next();
            if (client.getId().equalsIgnoreCase(clientID.trim())) return client;
        }
        return null;
    }

    // ---------------------------------------------------------------- input helpers

    private static String getToken(String prompt) {
        do {
            try {
                System.out.println(prompt);
                String line = reader.readLine();
                if (line == null) System.exit(0);
                StringTokenizer tokenizer = new StringTokenizer(line, "\n\r\f");
                if (tokenizer.hasMoreTokens()) return tokenizer.nextToken().trim();
            } catch (IOException ioe) {
                System.exit(0);
            }
        } while (true);
    }

    private static int getNumber(String prompt) {
        do {
            try {
                return Integer.parseInt(getToken(prompt));
            } catch (NumberFormatException nfe) {
                System.out.println("Please input a whole number");
            }
        } while (true);
    }

    private static double getDouble(String prompt) {
        do {
            try {
                return Double.parseDouble(getToken(prompt));
            } catch (NumberFormatException nfe) {
                System.out.println("Please input a number");
            }
        } while (true);
    }

    private static int getCommand() {
        do {
            try {
                int value = Integer.parseInt(getToken("\nEnter command (" + HELP + " for help):"));
                if (value >= EXIT && value <= HELP) return value;
            } catch (NumberFormatException nfe) {
                // fall through and ask again
            }
            System.out.println("Enter a number between " + EXIT + " and " + HELP);
        } while (true);
    }

    // ---------------------------------------------------------------- menu actions

    private static void help() {
        System.out.println("Person 2 interactive test - Product waitlist and Receive Shipment");
        System.out.println(EXIT + "  to exit");
        System.out.println(ADD_CLIENT + "  to add a client");
        System.out.println(ADD_PRODUCT + "  to add a product");
        System.out.println(PLACE_ORDER + "  to order a product for a client (fill from stock, waitlist the rest)");
        System.out.println(RECEIVE_SHIPMENT + "  to receive a shipment of a product");
        System.out.println(DISPLAY_PRODUCTS + "  to display products");
        System.out.println(DISPLAY_CLIENTS + "  to display clients and balances");
        System.out.println(DISPLAY_WAITLIST + "  to display a product's waitlist");
        System.out.println(DISPLAY_ORDERS + "  to display a client's invoices (orders)");
        System.out.println(RUN_CHECKS + "  to run the automated checks");
        System.out.println(HELP + " for help");
    }

    private static void addClient() {
        String name = getToken("Enter client name");
        String address = getToken("Enter address");
        String phone = getToken("Enter phone");
        try {
            System.out.println("Added " + warehouse.addClient(name, address, phone));
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add client: " + e.getMessage());
        }
    }

    private static void addProduct() {
        String name = getToken("Enter product name");
        int quantity = getNumber("Enter quantity in stock");
        double price = getDouble("Enter sale price");
        try {
            Product product = warehouse.addProduct(name, quantity, price);
            System.out.println(product == null ? "Product could not be added" : "Added " + product);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add product: " + e.getMessage());
        }
    }

    private static void placeOrder() {
        Client client = findClient(getToken("Enter client ID"));
        if (client == null) {
            System.out.println("No such client");
            return;
        }
        Product product = inventory.searchProduct(getToken("Enter product ID"));
        if (product == null) {
            System.out.println("No such product");
            return;
        }
        int requested = getNumber("Enter quantity");
        if (requested <= 0) {
            System.out.println("Quantity must be positive");
            return;
        }
        int filled = order(client, product, requested);
        System.out.printf("Filled %d from stock, charged $%.2f%n", filled, filled * product.getSalePrice());
        if (requested > filled) {
            System.out.println("Waitlisted " + (requested - filled) + " for " + client.getId());
        }
        System.out.println(product);
        System.out.println("Waitlist: [" + waitlistString(product) + "]");
    }

    private static void receiveShipment() {
        Product product = inventory.searchProduct(getToken("Enter product ID"));
        if (product == null) {
            System.out.println("No such product");
            return;
        }
        int quantity = getNumber("Enter quantity received");
        ShipmentResult result = receiveShipment(product, quantity);
        if (result == null) {
            System.out.println("Shipment rejected: quantity must be positive");
            return;
        }
        System.out.println("Shipment of " + quantity + " " + product.getId() + " received");
        Iterator orders = result.getOrders();
        if (!orders.hasNext()) System.out.println("No waitlisted clients");
        while (orders.hasNext()) {
            Order order = (Order) orders.next();
            System.out.printf("  Filled waitlist: %s charged $%.2f -> balance $%.2f%n",
                    order.getClient().getId(), order.getTotal(), order.getClient().getBalance());
        }
        System.out.println("Added " + result.getQuantityAddedToStock() + " to stock");
        System.out.println(result.getProduct());
        System.out.println("Waitlist: [" + waitlistString(product) + "]");
    }

    private static void displayProducts() {
        Iterator<Product> products = warehouse.getProducts();
        if (!products.hasNext()) System.out.println("No products");
        while (products.hasNext()) {
            Product product = products.next();
            System.out.println(product + " | waitlist: [" + waitlistString(product) + "]");
        }
    }

    private static void displayClients() {
        Iterator<Client> clients = warehouse.getClients();
        if (!clients.hasNext()) System.out.println("No clients");
        while (clients.hasNext()) System.out.println(clients.next());
    }

    private static void displayWaitlist() {
        Product product = inventory.searchProduct(getToken("Enter product ID"));
        if (product == null) {
            System.out.println("No such product");
            return;
        }
        if (!product.hasWaitlist()) {
            System.out.println("Waitlist for " + product.getId() + " is empty");
            return;
        }
        System.out.println("Waitlist for " + product.getId() + " (front first):");
        int position = 1;
        for (Iterator iterator = product.getWaitlist(); iterator.hasNext(); ) {
            System.out.println("  " + position++ + ". " + iterator.next());
        }
    }

    private static void displayOrders() {
        Client client = findClient(getToken("Enter client ID"));
        if (client == null) {
            System.out.println("No such client");
            return;
        }
        Iterator<Order> orders = client.getOrders();
        if (!orders.hasNext()) System.out.println("No orders for " + client.getId());
        while (orders.hasNext()) {
            Order order = orders.next();
            System.out.println(order);
            for (Iterator<OrderItem> items = order.getItems(); items.hasNext(); ) {
                System.out.println("    " + items.next());
            }
        }
    }

    private static void step(String description) {
        System.out.println();
        System.out.println(">> " + description);
    }

    private static void printWishlist(Client client) {
        Iterator<WishlistItem> items = client.getWishlist();
        if (!items.hasNext()) System.out.println("Wishlist for " + client.getId() + " is empty");
        while (items.hasNext()) System.out.println(items.next());
    }

    private static void printWaitlist(Product product) {
        if (!product.hasWaitlist()) System.out.println("Waitlist for " + product.getId() + " is empty");
        for (Iterator iterator = product.getWaitlist(); iterator.hasNext(); ) {
            System.out.println(iterator.next());
        }
    }

    private static void printOrder(Client client, Order invoice) {
        System.out.println(invoice == null ? "Nothing in stock; everything waitlisted" : "Invoice: " + invoice);
    }

    // Runs the required test script up to (not including) "Receive a shipment of 100 items of P1",
    // so the rest of the script can be demonstrated from the menu.
    private static void loadTestScript() {
        System.out.println("=== Required test script, steps before Receive Shipment ===");
        step("Create five clients: C1 through C5");
        Client c1 = warehouse.addClient("C1", "Address 1", "111");
        Client c2 = warehouse.addClient("C2", "Address 2", "222");
        Client c3 = warehouse.addClient("C3", "Address 3", "333");
        warehouse.addClient("C4", "Address 4", "444");
        warehouse.addClient("C5", "Address 5", "555");
        step("Display all clients");
        displayClients();

        step("Create five products: P1-P5, qty 10/20/30/40/50, price $1-$5");
        Product p1 = warehouse.addProduct("P1", 10, 1.0);
        Product p2 = warehouse.addProduct("P2", 20, 2.0);
        Product p3 = warehouse.addProduct("P3", 30, 3.0);
        Product p4 = warehouse.addProduct("P4", 40, 4.0);
        Product p5 = warehouse.addProduct("P5", 50, 5.0);
        step("Display all products");
        displayProducts();

        step("Add to C1's wishlist: 5 each of P1, P3 and P5");
        c1.addToWishlist(p1, 5);
        c1.addToWishlist(p3, 5);
        c1.addToWishlist(p5, 5);
        step("Print C1's wishlist");
        printWishlist(c1);
        step("Add to C2's wishlist: 7 each of P1, P2 and P4");
        c2.addToWishlist(p1, 7);
        c2.addToWishlist(p2, 7);
        c2.addToWishlist(p4, 7);
        step("Print C2's wishlist");
        printWishlist(c2);
        step("Add to C3's wishlist: 6 each of P1, P2 and P5");
        c3.addToWishlist(p1, 6);
        c3.addToWishlist(p2, 6);
        c3.addToWishlist(p5, 6);
        step("Print C3's wishlist");
        printWishlist(c3);

        step("Place order for C2, buy everything (if available) in wishlist");
        printOrder(c2, orderWholeWishlist(c2));
        step("Display all clients");
        displayClients();
        step("Place order for C3, buy everything (if available) in wishlist");
        printOrder(c3, orderWholeWishlist(c3));
        step("Display all clients");
        displayClients();
        step("Print C2's wishlist");
        printWishlist(c2);
        step("Print C3's wishlist");
        printWishlist(c3);
        step("Print P1's waitlist");
        printWaitlist(p1);
        step("Print P2's waitlist");
        printWaitlist(p2);

        step("Place order for C1, buy everything (if available) in wishlist");
        printOrder(c1, orderWholeWishlist(c1));
        step("Display all clients");
        displayClients();
        step("Print C1's wishlist");
        printWishlist(c1);

        step("Record receipt of $100 payment for C1 and C2");
        warehouse.receivePayment(c1.getId(), 100);
        warehouse.receivePayment(c2.getId(), 100);
        step("Display all clients");
        displayClients();

        System.out.println();
        System.out.println("=== Ready for Receive Shipment. Continue the script from the menu: ===");
        System.out.println("  " + RECEIVE_SHIPMENT + ", P1, 100  -> Receive a shipment of 100 items of P1");
        System.out.println("  " + DISPLAY_PRODUCTS + "            -> Display all products");
        System.out.println("  " + DISPLAY_CLIENTS + "            -> Display all clients");
        System.out.println("  " + DISPLAY_ORDERS + ", C1       -> Print all invoices for C1");
        System.out.println("  " + DISPLAY_ORDERS + ", C2       -> Print all invoices for C2");
    }

    // ---------------------------------------------------------------- automated checks

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
        System.out.println("PASS  " + message);
    }

    // Uses its own objects, not the Warehouse, so it does not disturb interactive data.
    private static void runAutomatedChecks() {
        checks = 0;
        Client c1 = new Client("C1", "Address 1", "111");
        Client c2 = new Client("C2", "Address 2", "222");
        Client c3 = new Client("C3", "Address 3", "333");
        Product p1 = new Product("P1", 10, 1.0);

        System.out.println("--- Required test sequence ---");
        order(c2, p1, 7);
        check(p1.getQuantityInStock() == 3 && !p1.hasWaitlist(), "after C2: stock 3, no waitlist");
        check(c2.getBalance() == 7.0, "C2 charged $7 at order time");

        order(c3, p1, 6);
        check(p1.getQuantityInStock() == 0, "after C3: stock 0");
        check(waitlistString(p1).equals("C3 x 3"), "after C3: waitlist = C3 x 3");
        check(c3.getBalance() == 3.0, "C3 charged only for the 3 filled");

        order(c1, p1, 5);
        check(p1.getQuantityInStock() == 0, "after C1: stock 0, never negative");
        check(waitlistString(p1).equals("C3 x 3, C1 x 5"), "after C1: waitlist C3 x 3 then C1 x 5 (FIFO)");
        check(c1.getBalance() == 0.0, "C1 not charged while waitlisted");

        ShipmentResult result = receiveShipment(p1, 100);
        check(c3.getBalance() == 6.0, "C3 charged $3 at shipment (balance 3 -> 6)");
        check(c1.getBalance() == 5.0, "C1 charged $5 at shipment");
        check(!p1.hasWaitlist(), "waitlist empty");
        check(p1.getQuantityInStock() == 92, "P1 stock 92");
        check(result.getQuantityAddedToStock() == 92, "result reports 92 to stock");
        check(c3.getOrders().hasNext() && c1.getOrders().hasNext(), "orders recorded on clients");

        System.out.println("--- Partial fill ---");
        Product p2 = new Product("P2", 0, 2.0);
        Client a = new Client("A", "Address", "1");
        Client b = new Client("B", "Address", "2");
        order(a, p2, 5);
        order(b, p2, 4);
        result = receiveShipment(p2, 3);
        check(waitlistString(p2).equals("A x 2, B x 4"), "partly filled entry stays at front, reduced");
        check(a.getBalance() == 6.0 && b.getBalance() == 0.0, "only A charged for 3 units");
        check(p2.getQuantityInStock() == 0 && result.getQuantityAddedToStock() == 0, "nothing left for stock");
        receiveShipment(p2, 10);
        check(!p2.hasWaitlist() && p2.getQuantityInStock() == 4, "next shipment finishes A then B, 4 to stock");
        check(a.getBalance() == 10.0 && b.getBalance() == 8.0, "A and B charged at fulfillment");

        System.out.println("--- Edge cases ---");
        check(p2.removeFromStock(10) == 4 && p2.getQuantityInStock() == 0, "removeFromStock caps at stock");
        check(p2.removeFromStock(3) == 0, "removeFromStock returns 0 when out of stock");
        check(p2.getFirstWaitlistItem() == null, "peek on empty waitlist returns null");
        check(receiveShipment(p2, 0) == null && receiveShipment(p2, -5) == null, "nonpositive shipment rejected");
        order(a, p2, 1);
        WaitlistItem first = p2.getFirstWaitlistItem();
        check(p2.getFirstWaitlistItem() == first && p2.hasWaitlist(), "peek does not remove");
        check(p2.removeWaitlistItem(first) && !p2.removeWaitlistItem(first), "remove specific entry once");

        System.out.println("All " + checks + " Person 2 checks passed.");
    }

    // ---------------------------------------------------------------- main

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--auto")) {
            runAutomatedChecks();
            return;
        }
        loadTestScript();
        System.out.println();
        help();
        int command;
        while ((command = getCommand()) != EXIT) {
            try {
                switch (command) {
                    case ADD_CLIENT:         addClient();          break;
                    case ADD_PRODUCT:        addProduct();         break;
                    case PLACE_ORDER:        placeOrder();         break;
                    case RECEIVE_SHIPMENT:   receiveShipment();    break;
                    case DISPLAY_PRODUCTS:   displayProducts();    break;
                    case DISPLAY_CLIENTS:    displayClients();     break;
                    case DISPLAY_WAITLIST:   displayWaitlist();    break;
                    case DISPLAY_ORDERS:     displayOrders();      break;
                    case RUN_CHECKS:         runAutomatedChecks(); break;
                    case HELP:               help();               break;
                }
            } catch (AssertionError e) {
                System.out.println("FAIL  " + e.getMessage());
            }
        }
        System.out.println("Goodbye");
    }
}
