import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WarehouseContext {
    public static final int NONE = -1;
    public static final int LOGIN = 0;
    public static final int CLIENT = 1;
    public static final int CLERK = 2;
    public static final int MANAGER = 3;

    private static final int TO_LOGIN = 0;
    private static final int TO_CLIENT = 1;
    private static final int TO_CLERK = 2;
    private static final int TO_MANAGER = 3;
    private static final int EXIT = 4;
    private static final int INVALID = -2;
    private static final int STOP = -1;

    private static WarehouseContext context;
    private final Warehouse warehouse;
    private final BufferedReader reader;
    private final WarehouseState[] states;
    private final int[][] nextState;
    private int currentState = LOGIN;
    private int loginType = NONE;
    private String clientID;
    private boolean running = true;

    private WarehouseContext() {
        warehouse = Warehouse.instance();
        reader = new BufferedReader(new InputStreamReader(System.in));
        states = new WarehouseState[] {
            LoginState.instance(), ClientMenuState.instance(),
            ClerkMenuState.instance(), ManagerMenuState.instance()
        };
        // Columns: TO_LOGIN, TO_CLIENT, TO_CLERK, TO_MANAGER, EXIT.
        nextState = new int[][] {
            {INVALID, CLIENT, CLERK, MANAGER, STOP},
            {LOGIN, INVALID, CLERK, INVALID, INVALID},
            {LOGIN, CLIENT, INVALID, MANAGER, INVALID},
            {LOGIN, INVALID, CLERK, INVALID, INVALID}
        };
    }

    public static WarehouseContext instance() {
        if (context == null) {
            context = new WarehouseContext();
        }
        return context;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public String getClientID() {
        return clientID;
    }

    public int getLoginType() {
        return loginType;
    }

    public boolean isRunning() {
        return running;
    }

    public String getToken(String prompt) {
        while (running) {
            System.out.println(prompt);
            try {
                String line = reader.readLine();
                if (line == null) {
                    running = false;
                    return null;
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    return line;
                }
                System.out.println("Please enter a value");
            } catch (IOException exception) {
                System.out.println("Unable to read input");
                running = false;
                return null;
            }
        }
        return null;
    }

    public int getNumber(String prompt) {
        String token;
        while ((token = getToken(prompt)) != null) {
            try {
                int value = Integer.parseInt(token);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException exception) {
                // Ask again below.
            }
            System.out.println("Enter a positive whole number");
        }
        return -1;
    }

    public double getDouble(String prompt) {
        String token;
        while ((token = getToken(prompt)) != null) {
            try {
                double value = Double.parseDouble(token);
                if (Double.isFinite(value) && value > 0) {
                    return value;
                }
            } catch (NumberFormatException exception) {
                // Ask again below.
            }
            System.out.println("Enter a positive finite amount");
        }
        return Double.NaN;
    }

    public boolean yesOrNo(String prompt) {
        String answer = getToken(prompt + " (y/n)");
        return answer != null && (answer.charAt(0) == 'y' || answer.charAt(0) == 'Y');
    }

    public boolean login(int role, String id) {
        if (!running || currentState != LOGIN) {
            return false;
        }
        int event;
        String validatedID = null;
        if (role == CLIENT) {
            if (id == null || (validatedID = id.trim()).isEmpty()
                    || warehouse.getClient(validatedID) == null) {
                return false;
            }
            event = TO_CLIENT;
        } else if (role == CLERK) {
            event = TO_CLERK;
        } else if (role == MANAGER) {
            event = TO_MANAGER;
        } else {
            return false;
        }
        if (!changeState(event)) {
            return false;
        }
        loginType = role;
        clientID = validatedID;
        return true;
    }

    public boolean becomeClient(String id) {
        if (!running || currentState != CLERK || id == null) {
            return false;
        }
        String validatedID = id.trim();
        if (validatedID.isEmpty() || warehouse.getClient(validatedID) == null) {
            return false;
        }
        if (!changeState(TO_CLIENT)) {
            return false;
        }
        clientID = validatedID;
        return true;
    }

    public boolean becomeClerk() {
        return currentState == MANAGER && changeState(TO_CLERK);
    }

    public void logout() {
        int event;
        if (currentState == CLIENT) {
            event = loginType == CLIENT ? TO_LOGIN : TO_CLERK;
        } else if (currentState == CLERK) {
            event = loginType == MANAGER ? TO_MANAGER : TO_LOGIN;
        } else if (currentState == MANAGER) {
            event = TO_LOGIN;
        } else {
            return;
        }
        if (changeState(event)) {
            clientID = null;
            if (currentState == LOGIN) {
                loginType = NONE;
            }
        }
    }

    public void exit() {
        if (currentState == LOGIN && changeState(EXIT)) {
            loginType = NONE;
            clientID = null;
            System.out.println("Goodbye");
        }
    }

    private boolean changeState(int event) {
        if (!running || event < 0 || event >= nextState[currentState].length) {
            return false;
        }
        int next = nextState[currentState][event];
        if (next == INVALID) {
            return false;
        }
        if (next == STOP) {
            running = false;
        } else {
            currentState = next;
        }
        return true;
    }

    public void process() {
        // Menus return here after changing state instead of nesting run calls.
        while (running) {
            states[currentState].run();
        }
    }

    public static void main(String[] args) {
        WarehouseContext.instance().process();
    }
}
