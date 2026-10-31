public class LoginState extends WarehouseState {
    private static final int CLIENT_LOGIN = 0;
    private static final int CLERK_LOGIN = 1;
    private static final int MANAGER_LOGIN = 2;
    private static final int EXIT = 3;
    private static LoginState state;

    private LoginState() {
        super();
    }

    public static LoginState instance() {
        if (state == null) {
            state = new LoginState();
        }
        return state;
    }

    private void help() {
        System.out.println("0 to login as client");
        System.out.println("1 to login as clerk");
        System.out.println("2 to login as manager");
        System.out.println("3 to exit");
    }

    private int getCommand() {
        String token;
        while ((token = context.getToken("Enter command")) != null) {
            try {
                int command = Integer.parseInt(token);
                if (command >= CLIENT_LOGIN && command <= EXIT) {
                    return command;
                }
            } catch (NumberFormatException exception) {
                // Ask again below.
            }
            System.out.println("Enter a number between 0 and 3");
        }
        return -1;
    }

    public void run() {
        context = WarehouseContext.instance();
        help();
        while (context.isRunning()) {
            int command = getCommand();
            if (command == -1) {
                return;
            }
            switch (command) {
                case CLIENT_LOGIN:
                    String id = context.getToken("Enter client ID");
                    if (id == null) {
                        return;
                    }
                    if (context.login(WarehouseContext.CLIENT, id)) {
                        return;
                    }
                    System.out.println("Invalid client ID");
                    break;
                case CLERK_LOGIN:
                    if (context.login(WarehouseContext.CLERK, null)) {
                        return;
                    }
                    break;
                case MANAGER_LOGIN:
                    if (context.login(WarehouseContext.MANAGER, null)) {
                        return;
                    }
                    break;
                case EXIT:
                    context.exit();
                    return;
                default:
                    break;
            }
            help();
        }
    }
}
