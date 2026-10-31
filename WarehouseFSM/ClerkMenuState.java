import java.util.Iterator;

public class ClerkMenuState extends WarehouseState {

    private static ClerkMenuState state;
    private Warehouse warehouse;

    private static final int LOGOUT = 0;
    private static final int ADD_CLIENT = 1;
    private static final int SHOW_PRODUCTS = 2;
    private static final int SHOW_CLIENTS = 3;
    private static final int SHOW_OUTSTANDING_BALANCES = 4;
    private static final int RECORD_PAYMENT = 5;
    private static final int BECOME_CLIENT = 6;
    private static final int HELP = 7;

    private ClerkMenuState() {
        super();
    }

    public static ClerkMenuState instance() {
        if (state == null) {
            state = new ClerkMenuState();
        }
        return state;
    }

    private int getCommand() {
        do {
            String token =
                context.getToken("Enter command: " + HELP + " for help");

            if (token == null) {
                return -1;
            }

            try {
                int value = Integer.parseInt(token);

                if (value >= LOGOUT && value <= HELP) {
                    return value;
                }

                System.out.println(
                    "Enter a number between "
                    + LOGOUT + " and " + HELP);
            }
            catch (NumberFormatException exception) {
                System.out.println("Enter a number");
            }

        } while (true);
    }

    public void help() {

        System.out.println(
            "Enter a number between "
            + LOGOUT + " and "
            + HELP + " as explained below:");

        System.out.println(LOGOUT + " to logout");
        System.out.println(ADD_CLIENT + " to add a client");
        System.out.println(SHOW_PRODUCTS + " to show products");
        System.out.println(SHOW_CLIENTS + " to show clients");
        System.out.println(
            SHOW_OUTSTANDING_BALANCES
            + " to show clients with outstanding balances");
        System.out.println(RECORD_PAYMENT + " to record payment");
        System.out.println(BECOME_CLIENT + " to become a client");
        System.out.println(HELP + " for help");
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
            System.out.println("Unable to add client");
            return;
        }

        System.out.println("Client successfully created");
        System.out.println("Client ID: " + client.getId());
    }

    public void showProducts() {

        Iterator<Product> products =
            warehouse.getProducts();

        if (!products.hasNext()) {
            System.out.println("No products available");
            return;
        }

        while (products.hasNext()) {
            System.out.println(products.next());
        }
    }

    public void showClients() {

        Iterator<Client> clients =
            warehouse.getClients();

        if (!clients.hasNext()) {
            System.out.println("No clients found");
            return;
        }

        while (clients.hasNext()) {
            System.out.println(clients.next());
        }
    }

    public void showClientsWithOutstandingBalance() {

        Iterator<Client> clients =
            warehouse.getClientsWithOutstandingBalance();

        if (!clients.hasNext()) {
            System.out.println(
                "No clients have an outstanding balance");
            return;
        }

        while (clients.hasNext()) {
            System.out.println(clients.next());
        }
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
            System.out.println(
                "Unable to record payment");
            return;
        }

        System.out.println("Payment recorded");
        System.out.printf(
            "Current balance: $%.2f%n",
            client.getBalance());
    }

    public void becomeClient() {

        String clientID =
            context.getToken("Enter client ID");

        if (clientID == null) {
            return;
        }

        if (!context.becomeClient(clientID)) {
            System.out.println("Invalid client ID");
        }
    }

    public void logout() {
        context.logout();
    }

    public void run() {

        context = WarehouseContext.instance();
        warehouse = context.getWarehouse();

        help();

        int command;

        while (true) {

            command = getCommand();

            if (command == -1) {
                return;
            }

            switch (command) {

                case LOGOUT:
                    logout();
                    return;

                case ADD_CLIENT:
                    addClient();
                    break;

                case SHOW_PRODUCTS:
                    showProducts();
                    break;

                case SHOW_CLIENTS:
                    showClients();
                    break;

                case SHOW_OUTSTANDING_BALANCES:
                    showClientsWithOutstandingBalance();
                    break;

                case RECORD_PAYMENT:
                    recordPayment();
                    break;

                case BECOME_CLIENT:
                    becomeClient();
                    return;

                case HELP:
                    help();
                    break;
            }
        }
    }
}
