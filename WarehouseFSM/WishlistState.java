import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * The wishlist state of the warehouse FSM, reached from the client menu through
 * "Perform wishlist operations". It holds the wishlist commands that used to be
 * in ClientMenuState (view, add, place an order) plus remove and change
 * quantity. Like the other GUI states, run() builds the screen and returns,
 * each button is its own class, and every operation works only on
 * context.getClientID(). "Back to client menu" asks WarehouseContext for the
 * transition.
 */
public class WishlistState extends WarehouseState {
  private static WishlistState state;
  private Warehouse warehouse;

  private WishlistState() {
    super();
  }

  /**
   * Returns the single WishlistState, creating it on first use.
   * @return the wishlist state
   */
  public static WishlistState instance() {
    if (state == null) {
      state = new WishlistState();
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

  // True when the active client's wishlist has no items; shows a message if so
  private boolean wishlistIsEmpty(String clientID) {
    Iterator<WishlistItem> wishlist = warehouse.getWishlist(clientID);
    if (wishlist == null || !wishlist.hasNext()) {
      context.showMessage("Wishlist is empty");
      return true;
    }
    return false;
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
   * Removes one product from the active client's wishlist.
   */
  public void removeFromWishlist() {
    String clientID = context.getClientID();
    if (wishlistIsEmpty(clientID)) {
      return;
    }
    String productID = context.getInput("Enter product ID to remove");
    if (productID == null) {
      return;
    }
    if (warehouse.removeFromWishlist(clientID, productID)) {
      context.showMessage(productID + " removed from wishlist");
    } else {
      context.showMessage(productID + " is not in the wishlist");
    }
  }

  /**
   * Changes the quantity of one product already in the active client's wishlist.
   */
  public void changeQuantity() {
    String clientID = context.getClientID();
    if (wishlistIsEmpty(clientID)) {
      return;
    }
    String productID = context.getInput("Enter product ID to change");
    if (productID == null) {
      return;
    }
    int quantity = getQuantity("Enter new quantity");
    if (quantity == -1) {
      return;
    }
    if (warehouse.changeWishlistQuantity(clientID, productID, quantity)) {
      context.showMessage("Quantity of " + productID + " changed to " + quantity);
    } else {
      context.showMessage(productID + " is not in the wishlist");
    }
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
    Map<String, Integer> filled = ClientMenuState.appendOrderItems(text, order);
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

  /**
   * Goes back to the client menu; WarehouseContext makes the transition.
   */
  public void backToClientMenu() {
    context.closeWishlist();
  }

  /**
   * Builds the wishlist screen on the shared frame and returns.
   */
  public void run() {
    context = WarehouseContext.instance();
    warehouse = context.getWarehouse();
    JFrame frame = context.getFrame();
    frame.getContentPane().removeAll();
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(new JLabel("Wishlist operations - " + context.getClientID()), BorderLayout.NORTH);
    JPanel buttons = new JPanel(new GridLayout(0, 1, 5, 5));
    buttons.add(new WishlistViewButton());
    buttons.add(new WishlistAddButton());
    buttons.add(new WishlistRemoveButton());
    buttons.add(new WishlistChangeButton());
    buttons.add(new WishlistOrderButton());
    buttons.add(new WishlistBackButton());
    frame.getContentPane().add(buttons, BorderLayout.CENTER);
    frame.setTitle("Warehouse - Wishlist");
    frame.revalidate();
    frame.repaint();
    frame.setVisible(true);
    frame.toFront();
  }
}
