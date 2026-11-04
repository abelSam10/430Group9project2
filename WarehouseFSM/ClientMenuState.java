import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * The GUI version of the client menu state of the warehouse FSM. It follows the
 * run() structure of Loginstate and the listener pattern of ClerkButton in
 * samplecode/Library: run() builds the screen and returns, and each button is
 * its own class that calls one method here. Every operation works only on the
 * client returned by context.getClientID(), and the choice of the next state
 * belongs to WarehouseContext.
 */
public class ClientMenuState extends WarehouseState {
  private static ClientMenuState state;
  private Warehouse warehouse;

  private ClientMenuState() {
    super();
  }

  /**
   * Returns the single ClientMenuState, creating it on first use.
   * @return the client menu state
   */
  public static ClientMenuState instance() {
    if (state == null) {
      state = new ClientMenuState();
    }
    return state;
  }

  // Reads a positive integer (a quantity); returns -1 on Cancel
  private int getQuantity(String prompt) {
    do {
      String token = context.getInput(prompt);
      if (token == null) {
        return -1;
      }
      try {
        int value = Integer.parseInt(token);
        if (value > 0) {
          return value;
        }
      } catch (NumberFormatException nfe) {
        // Ask again below
      }
      context.showMessage("Enter a whole number greater than 0");
    } while (true);
  }

  /**
   * Shows the active client's ID, name, address, phone and balance.
   */
  public void showClientDetails() {
    String clientID = context.getClientID();
    Client client = warehouse.getClient(clientID);
    if (client == null) {
      context.showMessage("No such client");
      return;
    }
    StringBuilder text = new StringBuilder();
    text.append("Client ID: ").append(client.getId()).append("\n");
    text.append("Name: ").append(client.getName()).append("\n");
    text.append("Address: ").append(client.getAddress()).append("\n");
    text.append("Phone: ").append(client.getPhone()).append("\n");
    text.append(String.format("Balance: $%.2f (positive = amount owed, negative = credit)",
        client.getBalance()));
    context.showOutput("Client details: " + clientID, text.toString());
  }

  /**
   * Lists every product with its ID, name and sale price (no quantity).
   */
  public void showProducts() {
    Iterator<Product> allProducts = warehouse.getProducts();
    if (!allProducts.hasNext()) {
      context.showOutput("Products", "No products");
      return;
    }
    StringBuilder text = new StringBuilder();
    while (allProducts.hasNext()) {
      Product product = allProducts.next();
      text.append(String.format("%-4s %-15s $%.2f",
          product.getId(), product.getName(), product.getSalePrice())).append("\n");
    }
    context.showOutput("Products", text.toString());
  }

  /**
   * Shows the active client's orders, each with its items and total, then the balance.
   */
  public void showTransactions() {
    String clientID = context.getClientID();
    Iterator<Order> orders = warehouse.getOrders(clientID);
    if (orders == null) {
      context.showMessage("No such client");
      return;
    }
    StringBuilder text = new StringBuilder();
    if (!orders.hasNext()) {
      text.append("No transactions\n");
    }
    int number = 1;
    while (orders.hasNext()) {
      Order order = orders.next();
      text.append("Order ").append(number++).append(":\n");
      appendOrderItems(text, order);
      text.append(String.format("  Order total: $%.2f", order.getTotal())).append("\n");
    }
    text.append(String.format("Current balance: $%.2f",
        warehouse.getClient(clientID).getBalance()));
    context.showOutput("Transactions: " + clientID, text.toString());
  }

  /**
   * Adds products to the active client's wishlist until the user says stop.
   */
  public void addToWishlist() {
    String clientID = context.getClientID();
    do {
      String productID = context.getInput("Enter product ID");
      if (productID == null) {
        return;
      }
      int quantity = getQuantity("Enter quantity");
      if (quantity == -1) {
        return;
      }
      if (warehouse.addToWishlist(clientID, productID, quantity)) {
        context.showMessage(quantity + " of " + productID + " added to wishlist");
      } else {
        context.showMessage(productID + " could not be added (unknown product)");
      }
    } while (context.confirm("Add more items?"));
  }

  /**
   * Shows each item in the active client's wishlist.
   */
  public void displayWishlist() {
    String clientID = context.getClientID();
    Iterator<WishlistItem> wishlist = warehouse.getWishlist(clientID);
    if (wishlist == null) {
      context.showMessage("No such client");
      return;
    }
    if (!wishlist.hasNext()) {
      context.showOutput("Wishlist: " + clientID, "Wishlist is empty");
      return;
    }
    StringBuilder text = new StringBuilder();
    while (wishlist.hasNext()) {
      WishlistItem item = wishlist.next();
      Product product = item.getProduct();
      text.append(product.getId()).append("  ").append(product.getName())
          .append("  qty ").append(item.getQuantity()).append("\n");
    }
    context.showOutput("Wishlist: " + clientID, text.toString());
  }

  /**
   * Orders the whole wishlist after confirmation and shows the result.
   */
  public void placeOrder() {
    String clientID = context.getClientID();
    Iterator<WishlistItem> wishlist = warehouse.getWishlist(clientID);
    if (wishlist == null) {
      context.showMessage("No such client");
      return;
    }
    // Snapshot of requested quantities, used only to report what was waitlisted
    Map<String, Integer> requested = new LinkedHashMap<String, Integer>();
    while (wishlist.hasNext()) {
      WishlistItem item = wishlist.next();
      requested.put(item.getProduct().getId(), item.getQuantity());
    }
    if (requested.isEmpty()) {
      context.showMessage("Order could not be processed (wishlist is empty)");
      return;
    }
    displayWishlist();
    if (!context.confirm("Place order for all items?")) {
      return;
    }
    Order order = warehouse.processOrder(clientID);
    if (order == null) {
      context.showMessage("Order could not be processed (wishlist is empty)");
      return;
    }
    StringBuilder text = new StringBuilder("Order summary:\n");
    Map<String, Integer> filled = appendOrderItems(text, order);
    text.append(String.format("Order total: $%.2f", order.getTotal())).append("\n");
    for (Map.Entry<String, Integer> entry : requested.entrySet()) {
      Integer got = filled.get(entry.getKey());
      int waitlisted = entry.getValue() - (got == null ? 0 : got);
      if (waitlisted > 0) {
        text.append(waitlisted).append(" unit(s) of ").append(entry.getKey())
            .append(" waitlisted (insufficient stock)\n");
      }
    }
    text.append(String.format("Updated balance: $%.2f",
        warehouse.getClient(clientID).getBalance()));
    context.showOutput("Order placed: " + clientID, text.toString());
  }

  // Appends an order's items to text; returns product ID -> quantity filled
  private Map<String, Integer> appendOrderItems(StringBuilder text, Order order) {
    Map<String, Integer> filled = new HashMap<String, Integer>();
    Iterator<OrderItem> items = order.getItems();
    while (items.hasNext()) {
      OrderItem item = items.next();
      String productID = item.getProduct().getId();
      text.append(String.format("  %s  qty %d  @ $%.2f  = $%.2f",
          productID, item.getQuantity(), item.getPrice(), item.getCost())).append("\n");
      Integer before = filled.get(productID);
      filled.put(productID, (before == null ? 0 : before) + item.getQuantity());
    }
    return filled;
  }

  /**
   * Logs out; WarehouseContext decides the next state and shows it.
   */
  public void logout() {
    context.logout();
  }

  /**
   * Builds the client menu screen on the shared frame and returns.
   */
  public void run() {
    context = WarehouseContext.instance();
    warehouse = context.getWarehouse();
    JFrame frame = context.getFrame();
    frame.getContentPane().removeAll();
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(new JLabel("Client menu - " + context.getClientID()), BorderLayout.NORTH);
    JPanel buttons = new JPanel(new GridLayout(0, 1, 5, 5));
    buttons.add(new ClientDetailsButton());
    buttons.add(new ClientProductsButton());
    buttons.add(new ClientTransactionsButton());
    buttons.add(new ClientAddWishlistButton());
    buttons.add(new ClientWishlistButton());
    buttons.add(new ClientOrderButton());
    buttons.add(new ClientLogoutButton());
    frame.getContentPane().add(buttons, BorderLayout.CENTER);
    frame.setTitle("Warehouse - Client");
    frame.revalidate();
    frame.repaint();
    frame.setVisible(true);
    frame.toFront();
  }
}
