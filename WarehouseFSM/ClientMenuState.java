import java.util.*;

/**
 * The client menu state of the warehouse FSM. It corresponds to Userstate in
 * the LibTextFSM sample. Every operation works only on the client returned by
 * context.getClientID(); this class never asks for a client ID. When the user
 * logs out it calls context.logout() and returns, leaving the choice of the
 * next state to WarehouseContext.
 */
public class ClientMenuState extends WarehouseState {
  private static ClientMenuState state;
  private Warehouse warehouse;

  // Local menu numbers, NOT Context events
  private static final int LOGOUT = 0;
  private static final int SHOW_DETAILS = 1;
  private static final int SHOW_PRODUCTS = 2;
  private static final int SHOW_TRANSACTIONS = 3;
  private static final int ADD_TO_WISHLIST = 4;
  private static final int DISPLAY_WISHLIST = 5;
  private static final int PLACE_ORDER = 6;
  private static final int HELP = 7;

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

  // Reads a menu command; returns -1 at end of input
  private int getCommand() {
    do {
      String token = context.getToken("Enter command: " + HELP + " for help");
      if (token == null) {
        return -1;
      }
      try {
        int value = Integer.parseInt(token);
        if (value >= LOGOUT && value <= HELP) {
          return value;
        }
        System.out.println("Enter a number between " + LOGOUT + " and " + HELP);
      } catch (NumberFormatException nfe) {
        System.out.println("Enter a number");
      }
    } while (true);
  }

  // Reads a positive integer (a quantity); returns -1 at end of input
  private int getNumber(String prompt) {
    do {
      String token = context.getToken(prompt);
      if (token == null) {
        return -1;
      }
      try {
        int value = Integer.parseInt(token);
        if (value > 0) {
          return value;
        }
        System.out.println("Please input a number greater than 0");
      } catch (NumberFormatException nfe) {
        System.out.println("Please input a number");
      }
    } while (true);
  }

  // True only for an answer starting with Y or y; end of input counts as no
  private boolean yesOrNo(String prompt) {
    String more = context.getToken(prompt + " (Y|y)[es] or anything else for no");
    if (more == null) {
      return false;
    }
    return more.charAt(0) == 'y' || more.charAt(0) == 'Y';
  }

  /**
   * Prints the client menu commands.
   */
  public void help() {
    System.out.println("Enter a number between " + LOGOUT + " and " + HELP + " as explained below:");
    System.out.println(LOGOUT + " to logout");
    System.out.println(SHOW_DETAILS + " to show client details");
    System.out.println(SHOW_PRODUCTS + " to show products");
    System.out.println(SHOW_TRANSACTIONS + " to show transactions");
    System.out.println(ADD_TO_WISHLIST + " to add items to the wishlist");
    System.out.println(DISPLAY_WISHLIST + " to display the wishlist");
    System.out.println(PLACE_ORDER + " to place an order");
    System.out.println(HELP + " for help");
  }

  /**
   * Shows the active client's ID, name, address, phone and balance.
   */
  public void showClientDetails() {
    String clientID = context.getClientID();
    Client client = warehouse.getClient(clientID);
    if (client == null) {
      System.out.println("No such client");
      return;
    }
    System.out.println("Client ID: " + client.getId());
    System.out.println("Name:      " + client.getName());
    System.out.println("Address:   " + client.getAddress());
    System.out.println("Phone:     " + client.getPhone());
    System.out.println(String.format("Balance:   $%.2f", client.getBalance())
        + " (positive = amount owed, negative = credit)");
  }

  /**
   * Lists every product with its ID, name and sale price (no quantity).
   */
  public void showProducts() {
    Iterator<Product> allProducts = warehouse.getProducts();
    if (!allProducts.hasNext()) {
      System.out.println("No products");
      return;
    }
    while (allProducts.hasNext()) {
      Product product = allProducts.next();
      System.out.println(String.format("%s  %s  $%.2f",
          product.getId(), product.getName(), product.getSalePrice()));
    }
  }

  /**
   * Shows the active client's orders, each with its items and total, then the balance.
   */
  public void showTransactions() {
    String clientID = context.getClientID();
    Iterator<Order> orders = warehouse.getOrders(clientID);
    if (orders == null) {
      System.out.println("No such client");
      return;
    }
    if (!orders.hasNext()) {
      System.out.println("No transactions");
    } else {
      System.out.println("Transactions for " + clientID + ":");
      int number = 1;
      while (orders.hasNext()) {
        Order order = orders.next();
        System.out.println("Order " + number++ + ":");
        printOrderItems(order);
        System.out.println(String.format("  Order total: $%.2f", order.getTotal()));
      }
    }
    System.out.println(String.format("Current balance: $%.2f",
        warehouse.getClient(clientID).getBalance()));
  }

  /**
   * Adds products to the active client's wishlist until the user says stop.
   */
  public void addToWishlist() {
    String clientID = context.getClientID();
    do {
      String productID = context.getToken("Enter product ID");
      if (productID == null) {
        return;
      }
      int quantity = getNumber("Enter quantity");
      if (quantity == -1) {
        return;
      }
      if (warehouse.addToWishlist(clientID, productID, quantity)) {
        System.out.println(quantity + " of " + productID + " added to wishlist");
      } else {
        System.out.println(productID + " could not be added (unknown product)");
      }
    } while (yesOrNo("Add more items?"));
  }

  /**
   * Shows each item in the active client's wishlist.
   */
  public void displayWishlist() {
    String clientID = context.getClientID();
    Iterator<WishlistItem> wishlist = warehouse.getWishlist(clientID);
    if (wishlist == null) {
      System.out.println("No such client");
      return;
    }
    System.out.println("Wishlist for " + clientID);
    if (!wishlist.hasNext()) {
      System.out.println("Wishlist is empty");
      return;
    }
    while (wishlist.hasNext()) {
      WishlistItem item = wishlist.next();
      Product product = item.getProduct();
      System.out.println(product.getId() + "  " + product.getName() + "  qty " + item.getQuantity());
    }
  }

  /**
   * Orders the whole wishlist after confirmation and shows the result.
   */
  public void placeOrder() {
    String clientID = context.getClientID();
    displayWishlist();
    if (!yesOrNo("Place order for all items?")) {
      return;
    }
    // Snapshot of requested quantities, used only to report what was waitlisted
    Map<String, Integer> requested = new LinkedHashMap<String, Integer>();
    Iterator<WishlistItem> wishlist = warehouse.getWishlist(clientID);
    if (wishlist != null) {
      while (wishlist.hasNext()) {
        WishlistItem item = wishlist.next();
        requested.put(item.getProduct().getId(), item.getQuantity());
      }
    }
    Order order = warehouse.processOrder(clientID);
    if (order == null) {
      System.out.println("Order could not be processed (wishlist is empty)");
      return;
    }
    System.out.println("Order summary:");
    Map<String, Integer> filled = printOrderItems(order);
    System.out.println(String.format("Order total: $%.2f", order.getTotal()));
    for (Map.Entry<String, Integer> entry : requested.entrySet()) {
      Integer got = filled.get(entry.getKey());
      int waitlisted = entry.getValue() - (got == null ? 0 : got);
      if (waitlisted > 0) {
        System.out.println(waitlisted + " unit(s) of " + entry.getKey() + " waitlisted (insufficient stock)");
      }
    }
    System.out.println(String.format("Updated balance: $%.2f",
        warehouse.getClient(clientID).getBalance()));
  }

  // Prints an order's items; returns product ID -> quantity filled
  private Map<String, Integer> printOrderItems(Order order) {
    Map<String, Integer> filled = new HashMap<String, Integer>();
    Iterator<OrderItem> items = order.getItems();
    while (items.hasNext()) {
      OrderItem item = items.next();
      String productID = item.getProduct().getId();
      System.out.println(String.format("  %s  qty %d  @ $%.2f  = $%.2f",
          productID, item.getQuantity(), item.getPrice(), item.getCost()));
      Integer before = filled.get(productID);
      filled.put(productID, (before == null ? 0 : before) + item.getQuantity());
    }
    return filled;
  }

  /**
   * Logs out; WarehouseContext decides the next state.
   */
  public void logout() {
    context.logout();
  }

  /**
   * Runs the client menu until logout or end of input.
   */
  public void run() {
    context = WarehouseContext.instance();
    warehouse = context.getWarehouse();
    help();
    int command;
    while (true) {
      command = getCommand();
      if (command == -1) return;            // end of input: Context already stopped; do NOT log out
      switch (command) {
        case LOGOUT:            logout(); return;   // back to Context's dispatch loop
        case SHOW_DETAILS:      showClientDetails(); break;
        case SHOW_PRODUCTS:     showProducts(); break;
        case SHOW_TRANSACTIONS: showTransactions(); break;
        case ADD_TO_WISHLIST:   addToWishlist(); break;
        case DISPLAY_WISHLIST:  displayWishlist(); break;
        case PLACE_ORDER:       placeOrder(); break;
        case HELP:              help(); break;
      }
    }
  }
}
