import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * The GUI version of the client menu state of the warehouse FSM. It follows the
 * run() structure of Loginstate and the listener pattern of ClerkButton in
 * samplecode/Library: run() builds the screen and returns, and each button is
 * its own class that calls one method here. Every operation works only on the
 * client returned by context.getClientID(), and the choice of the next state
 * belongs to WarehouseContext. The wishlist commands live in WishlistState,
 * which is reached through "Perform wishlist operations".
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

  // Appends an order's items to text; returns product ID -> quantity filled.
  // Also used by WishlistState for the order summary.
  static Map<String, Integer> appendOrderItems(StringBuilder text, Order order) {
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
   * Moves to the wishlist state; WarehouseContext makes the transition.
   */
  public void wishlistOperations() {
    context.openWishlist();
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
    buttons.add(new ClientWishlistOpsButton());
    buttons.add(new ClientLogoutButton());
    frame.getContentPane().add(buttons, BorderLayout.CENTER);
    frame.setTitle("Warehouse - Client");
    frame.revalidate();
    frame.repaint();
    frame.setVisible(true);
    frame.toFront();
  }
}
