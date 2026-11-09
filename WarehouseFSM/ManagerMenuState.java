import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

public class ManagerMenuState extends WarehouseState {
    private static ManagerMenuState instance;
    private static Warehouse warehouse;
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
        return context.confirm(prompt);
    }

    public int getNumber(String prompt) {
        do {
            try {
                String item = context.getInput(prompt);
                if (item == null) {
                    return -1;
                }
                int num = Integer.parseInt(item);
                if (num > 0) {
                    return num;
                }
                context.showMessage("Please input a number greater than 0");
            } catch (NumberFormatException nfe) {
                context.showMessage("Please input a number");
            }
        } while (true);
    }
    
    public double getDouble(String prompt) {
        do {
            try {
                String item = context.getInput(prompt);
                if (item == null) {
                    return -1;
                }
                double num = Double.parseDouble(item);
                if (Double.isFinite(num) && num > 0) {
                    return num;
                }
                context.showMessage("Please input a number greater than 0");
            } catch (NumberFormatException nfe) {
                context.showMessage("Please input a number");
            }
        } while (true);
    }

    public void addProducts() {
        Product result;
        do {
            String name = context.getInput("Enter name of product");
            if (name == null) {
                return;
            }
            int quantity = getNumber("Enter quantity");
            if (quantity == -1) {
                return;
            }
            double salePrice = getDouble("Enter sale price");
            if (name == null || quantity == -1 || salePrice == -1) {
                return;
            }
            result = warehouse.addProduct(name, quantity, salePrice);
            if (result != null) {
                context.showMessage(result.toString());
            } else {
                context.showMessage("Product could not be added");
            }
            if (!yesOrNo("Add more products?")) {
                break;
            }
        } while (true);
    }

    public void displayWaitlist() {
        String productID = context.getInput("Enter product id");
        if (productID == null) {
            return;
        }
        Iterator waitlist = warehouse.getWaitlist(productID);
        if (waitlist == null) {
            context.showMessage("No such product");
            return;
        }
        if (!waitlist.hasNext()) {
            context.showMessage("No waitlist for this product");
            return;
        }
        StringBuilder text = new StringBuilder();
        while (waitlist.hasNext()) {
            text.append(waitlist.next()).append("\n");
        }
        context.showOutput("Waitlist for product " + productID + ":", text.toString());
    }

    // the backend fills the waitlist first and puts the rest into stock
    public void receiveShipment() {
        ShipmentResult result;
        do {
            String productID = context.getInput("Enter product id");
            if (productID == null) {
                return;
            }
            int quantity = getNumber("Enter quantity received");
            if (productID == null || quantity == -1) {
                return;
            }
            result = warehouse.receiveShipment(productID, quantity);
            if (result == null) {
                context.showMessage("Invalid product or quantity");
            } else {
                StringBuilder text = new StringBuilder(); //better way of displaying this
                Iterator orders = result.getOrders();
                while (orders.hasNext()) {
                    Order order = (Order) orders.next();
                    text.append(order).append("\n");
                    Iterator items = order.getItems();
                    while (items.hasNext()) {
                        text.append("  ").append(items.next()).append("\n");
                    }
                }
                text.append("Quantity added to stock: " + result.getQuantityAddedToStock());
                context.showOutput("Shipment processed", text.toString());
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
        context.showMessage("Could not switch to the clerk menu");
        return false;
    }

    public void logout() {
        context.logout(); // Context picks the next state --> login menu
    }
    // build the manager screen
    public void run() {
        context = WarehouseContext.instance();
        JFrame frame = context.getFrame();
        frame.getContentPane().removeAll();
        frame.getContentPane().setLayout(new GridLayout(0, 1, 5, 5));
        frame.getContentPane().add(new ManagerAddProductsButton());
        frame.getContentPane().add(new ManagerWaitlistButton());
        frame.getContentPane().add(new ManagerShipmentButton());
        frame.getContentPane().add(new ManagerBecomeClerkButton());
        frame.getContentPane().add(new ManagerLogoutButton());
        frame.setTitle("Warehouse - Manager");
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
        frame.toFront();
    }
}
