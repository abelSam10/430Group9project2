import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class WarehouseContext {
    public static final int NONE = -1;
    public static final int LOGIN = 0;
    public static final int CLIENT = 1;
    public static final int CLERK = 2;
    public static final int MANAGER = 3;
    public static final int WISHLIST = 4;

    private static final int TO_LOGIN = 0;
    private static final int TO_CLIENT = 1;
    private static final int TO_CLERK = 2;
    private static final int TO_MANAGER = 3;
    private static final int EXIT = 4;
    private static final int TO_WISHLIST = 5;
    private static final int INVALID = -2;
    private static final int STOP = -1;

    private static WarehouseContext context;
    private final Warehouse warehouse;
    private final JFrame frame;
    private final WarehouseState[] states;
    private final int[][] nextState;
    private int currentState = LOGIN;
    private int loginType = NONE;
    private String clientID;
    private boolean running = true;

    private WarehouseContext() {
        warehouse = Warehouse.instance();
        frame = new JFrame("Warehouse - Login");
        frame.setSize(560, 460);
        frame.setMinimumSize(new java.awt.Dimension(440, 320));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                terminate();
            }
        });
        states = new WarehouseState[] {
            LoginState.instance(), ClientMenuState.instance(),
            ClerkMenuState.instance(), ManagerMenuState.instance(),
            WishlistState.instance()
        };
        // Columns: TO_LOGIN, TO_CLIENT, TO_CLERK, TO_MANAGER, EXIT, TO_WISHLIST.
        // Rows: LOGIN, CLIENT, CLERK, MANAGER, WISHLIST.
        nextState = new int[][] {
            {INVALID, CLIENT, CLERK, MANAGER, STOP, INVALID},
            {LOGIN, INVALID, CLERK, INVALID, INVALID, WISHLIST},
            {LOGIN, CLIENT, INVALID, MANAGER, INVALID, INVALID},
            {LOGIN, INVALID, CLERK, INVALID, INVALID, INVALID},
            {INVALID, CLIENT, INVALID, INVALID, INVALID, INVALID}
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

    public JFrame getFrame() {
        return frame;
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

    public String getInput(String prompt) {
        while (running) {
            String input = JOptionPane.showInputDialog(frame, prompt,
                    "Warehouse input", JOptionPane.QUESTION_MESSAGE);
            if (input == null || !running) {
                return null;
            }
            input = input.trim();
            if (!input.isEmpty()) {
                return input;
            }
            showMessage("Please enter a value");
        }
        return null;
    }

    public void showMessage(String message) {
        if (running) {
            JOptionPane.showMessageDialog(frame, message,
                    "Warehouse", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void showOutput(String title, String text) {
        if (!running) {
            return;
        }
        JTextArea output = new JTextArea(text == null ? "" : text, 18, 60);
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        output.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(output);
        JOptionPane.showMessageDialog(frame, scroll, title,
                JOptionPane.PLAIN_MESSAGE);
    }

    public boolean confirm(String question) {
        return running && JOptionPane.showConfirmDialog(frame, question,
                "Warehouse", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public int getNumber(String prompt) {
        String token;
        while ((token = getInput(prompt)) != null) {
            try {
                int value = Integer.parseInt(token);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException exception) {
                // Ask again below.
            }
            showMessage("Enter a positive whole number");
        }
        return -1;
    }

    public double getDouble(String prompt) {
        String token;
        while ((token = getInput(prompt)) != null) {
            try {
                double value = Double.parseDouble(token);
                if (Double.isFinite(value) && value > 0) {
                    return value;
                }
            } catch (NumberFormatException exception) {
                // Ask again below.
            }
            showMessage("Enter a positive finite amount");
        }
        return Double.NaN;
    }

    // Compatibility for menus that have not yet replaced the text helpers.
    public String getToken(String prompt) {
        return getInput(prompt);
    }

    public boolean yesOrNo(String prompt) {
        return confirm(prompt);
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
        // The next screen must see the new session values.
        loginType = role;
        clientID = validatedID;
        return changeState(event);
    }

    public boolean becomeClient(String id) {
        if (!running || currentState != CLERK || id == null) {
            return false;
        }
        String validatedID = id.trim();
        if (validatedID.isEmpty() || warehouse.getClient(validatedID) == null) {
            return false;
        }
        clientID = validatedID;
        return changeState(TO_CLIENT);
    }

    public boolean becomeClerk() {
        return currentState == MANAGER && changeState(TO_CLERK);
    }

    // The wishlist state works on the same client, so clientID and loginType stay as they are.
    public boolean openWishlist() {
        return currentState == CLIENT && changeState(TO_WISHLIST);
    }

    public boolean closeWishlist() {
        return currentState == WISHLIST && changeState(TO_CLIENT);
    }

    public void logout() {
        if (!running) {
            return;
        }
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
        clientID = null;
        if (event == TO_LOGIN) {
            loginType = NONE;
        }
        changeState(event);
    }

    public void exit() {
        if (currentState == LOGIN) {
            changeState(EXIT);
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
            terminate();
        } else {
            currentState = next;
            states[currentState].run();
        }
        return true;
    }

    private void terminate() {
        running = false;
        clientID = null;
        loginType = NONE;
        frame.dispose();
    }

    public void process() {
        if (running) {
            states[currentState].run();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> WarehouseContext.instance().process());
    }
}

