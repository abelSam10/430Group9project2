import java.util.Iterator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ClerkMenuState extends WarehouseState {

    private static ClerkMenuState state;
    private Warehouse warehouse;

    private ClerkMenuState() {
        super();
    }

    public static ClerkMenuState instance() {
        if (state == null) {
            state = new ClerkMenuState();
        }
        return state;
    }

    public void addClient() {

        String name =
            context.getToken("Enter client name");

        if (name == null) {
            return;
        }

        String address =
            context.getToken("Enter client address");

        if (address == null) {
            return;
        }

        String phone =
            context.getToken("Enter client phone");

        if (phone == null) {
            return;
        }

        Client client =
            warehouse.addClient(name, address, phone);

        if (client == null) {
            context.showMessage("Unable to add client");
            return;
        }

        context.showMessage("Client successfully created\nClient ID: " + client.getId());
    }

    public void showProducts() {

    Iterator<Product> products =
        warehouse.getProducts();

    if (!products.hasNext()) {
        context.showMessage("No products available");
        return;
    }

    StringBuilder output =
        new StringBuilder();

    while (products.hasNext()) {
        output.append(products.next())
              .append("\n");
    }

    context.showOutput(
        "Products",
        output.toString());
    }

    public void showClients() {

    Iterator<Client> clients =
        warehouse.getClients();

    if (!clients.hasNext()) {
        context.showMessage("No clients found");
        return;
    }

    StringBuilder output =
        new StringBuilder();

    while (clients.hasNext()) {
        output.append(clients.next())
              .append("\n");
    }

    context.showOutput(
        "Clients",
        output.toString());
    }

    public void showClientsWithOutstandingBalance() {

    Iterator<Client> clients =
        warehouse.getClientsWithOutstandingBalance();

    if (!clients.hasNext()) {
        context.showMessage(
            "No clients have an outstanding balance");
        return;
    }

    StringBuilder output =
        new StringBuilder();

    while (clients.hasNext()) {
        output.append(clients.next())
              .append("\n");
    }

    context.showOutput(
        "Outstanding Balances",
        output.toString());
    }

    public void recordPayment() {

        String clientID =
            context.getToken("Enter client ID");

        if (clientID == null) {
            return;
        }

        double payment =
            context.getDouble("Enter payment amount");

        if (Double.isNaN(payment)) {
            return;
        }

        Client client =
            warehouse.receivePayment(clientID, payment);

        if (client == null) {
            context.showMessage("Unable to record payment");
            return;
        }

        context.showMessage(String.format("Payment recorded\nCurrent balance: $%.2f",
            client.getBalance()));
    }

    public void becomeClient() {

        String clientID =
            context.getToken("Enter client ID");

        if (clientID == null) {
            return;
        }

        if (!context.becomeClient(clientID)) {
            context.showMessage("Invalid client ID");
        }
    }

    public void logout() {
        context.logout();
    }

    public void run() {
    context = WarehouseContext.instance();
    warehouse = context.getWarehouse();

    JFrame frame = context.getFrame();

    frame.getContentPane().removeAll();
    frame.getContentPane().setLayout(
        new BorderLayout(10, 10));

    frame.getContentPane().add(
        new JLabel("Clerk Menu",
            SwingConstants.CENTER),
        BorderLayout.NORTH);

    JPanel buttons =
        new JPanel(new GridLayout(0, 1, 8, 8));

    buttons.setBorder(
        BorderFactory.createEmptyBorder(
            16, 16, 16, 16));

    buttons.add(new ClerkAddClientButton());
    buttons.add(new ClerkShowProductsButton());
    buttons.add(new ClerkShowClientsButton());
    buttons.add(new ClerkOutstandingBalancesButton());
    buttons.add(new ClerkRecordPaymentButton());
    buttons.add(new ClerkBecomeClientButton());
    buttons.add(new ClerkLogoutButton());

    frame.getContentPane().add(
        buttons,
        BorderLayout.CENTER);

    frame.setTitle("Warehouse - Clerk");

    frame.revalidate();
    frame.repaint();
    frame.setVisible(true);
    }
}
