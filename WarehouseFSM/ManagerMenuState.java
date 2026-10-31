import java.util.*;
import java.io.*;


public class ManagerMenuState extends WarehouseState {
    private static ManagerMenuState instance;
    private static Warehouse warehouse;
    private static final int LOGOUT = 0;
    private static final int ADD_PRODUCTS = 1;
    private static final int DISPLAY_WAITLIST = 2;
    private static final int RECEIVE_SHIPMENT = 3;
    private static final int BECOME_CLERK = 4;
    private static final int HELP = 5;
    private ManagerMenuState() {
            super();
            warehouse = Warehouse.instance();
    }

    public static ManagerMenuState instance() {
        if (instance == null) {
            instance = new ManagerMenuState();
        }
        return instance;
    }
    private boolean yesOrNo(String prompt) {
        String more = context.getToken(prompt + " (Y|y)[es] or anything else for no");
        if (more == null || (more.charAt(0) != 'y' && more.charAt(0) != 'Y')) {
            return false;
        }
        return true;
    }
    public int getNumber(String prompt) {
        do {
            try {
                String item = context.getToken(prompt);
                if (item == null) {
                    return -1;
                }
                int num = Integer.parseInt(item);
                if (num > 0) {
                    return num;
                }
                System.out.println("Please input a number greater than 0");
            } catch (NumberFormatException nfe) {
                System.out.println("Please input a number ");
            }
        } while (true);
    }
    public double getDouble(String prompt) {
        do {
            try {
                String item = context.getToken(prompt);
                if (item == null) {
                    return -1;
                }
                double num = Double.parseDouble(item);
                if (num > 0) {
                    return num;
                }
                System.out.println("Please input a number greater than 0");
            } catch (NumberFormatException nfe) {
                System.out.println("Please input a number ");
            }
        } while (true);
    }
    public int getCommand() {
        do {
            try {
                String item = context.getToken("Enter command:" + HELP + " for help");
                if (item == null) {
                    return -1;
                }
                int value = Integer.parseInt(item);
                if (value >= LOGOUT && value <= HELP) {
                    return value;
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Enter a number");
            }
        } while (true);
    }

    public void help() {
        System.out.println("Enter a number between 0 and 5:");
        System.out.println(LOGOUT + " to Logout\n");
        System.out.println(ADD_PRODUCTS + " to  add products");
        System.out.println(DISPLAY_WAITLIST + " to  display a product's waitlist");
        System.out.println(RECEIVE_SHIPMENT + " to  receive a shipment");
        System.out.println(BECOME_CLERK + " to  switch to the clerk menu");
        System.out.println(HELP + " for help");
    }

    public void addProducts() {
        Product result;
        do {
            String name = context.getToken("Enter name of product");
            int quantity = getNumber("Enter quantity");
            double salePrice = getDouble("Enter sale price");
            if (name == null || quantity == -1 || salePrice == -1) {
                return;
            }
            result = warehouse.addProduct(name, quantity, salePrice);
            if (result != null) {
                System.out.println(result);
            } else {
                System.out.println("Product could not be added");
            }
            if (!yesOrNo("Add more products?")) {
                break;
            }
        } while (true);
    }

    public void displayWaitlist() {
        String productID = context.getToken("Enter product id");
        if (productID == null) {
            return;
        }
        Iterator waitlist = warehouse.getWaitlist(productID);
        if (waitlist == null) {
            System.out.println("No such product");
            return;
        }
        if (!waitlist.hasNext()) {
            System.out.println("No waitlist for this product");
            return;
        }
        System.out.println("Waitlist for product " + productID + ":");
        while (waitlist.hasNext()) {
            System.out.println(waitlist.next());
        }
    }

    // the backend fills the waitlist first and puts the rest into stock
    public void receiveShipment() {
        ShipmentResult result;
        do {
            String productID = context.getToken("Enter product id");
            int quantity = getNumber("Enter quantity received");
            if (productID == null || quantity == -1) {
                return;
            }
            result = warehouse.receiveShipment(productID, quantity);
            if (result == null) {
                System.out.println("Invalid product or quantity");
            } else {
                System.out.println("Shipment processed");
                Iterator orders = result.getOrders();
                while (orders.hasNext()) {
                    Order order = (Order) orders.next();
                    System.out.println(order);
                    Iterator items = order.getItems();
                    while (items.hasNext()) {
                        System.out.println("  " + items.next());
                    }
                }
                System.out.println("Quantity added to stock: " + result.getQuantityAddedToStock());
            }
            if (!yesOrNo("Receive more shipments?")) {
                break;
            }
        } while (true);
    }

    // Context keeps the manager login, so the clerk menu logs out back to here
    public boolean becomeClerk() {
        if (context.becomeClerk()) {
            return true;
        }
        System.out.println("Could not switch to the clerk menu");
        return false;
    }

    public void logout()
    {
        context.logout(); // Context picks the next state --> login menu
    }

    public void process() {
        int command;
        help();
        while ((command = getCommand()) != LOGOUT) {
            if (command == -1) {
                return; // end of input
            }
            switch (command) {
                case ADD_PRODUCTS:
                    addProducts();
                    break;
                case DISPLAY_WAITLIST:
                    displayWaitlist();
                    break;
                case RECEIVE_SHIPMENT:
                    receiveShipment();
                    break;
                case BECOME_CLERK:
                    if (becomeClerk()) {
                        return; // back to Context, which runs the clerk menu
                    }
                    break;
                case HELP:
                    help();
                    break;
            }
        }
        logout();
    }

    public void run() {
        context = WarehouseContext.instance();
        process();
    }
}