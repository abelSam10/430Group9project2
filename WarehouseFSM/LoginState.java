import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class LoginState extends WarehouseState {
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

    public void loginClient() {
        String id = context.getInput("Enter client ID");
        if (id == null) {
            return;
        }
        if (!context.login(WarehouseContext.CLIENT, id)) {
            context.showMessage("Invalid client ID");
        }
    }

    public void loginClerk() {
        context.login(WarehouseContext.CLERK, null);
    }

    public void loginManager() {
        context.login(WarehouseContext.MANAGER, null);
    }

    public void exit() {
        context.exit();
    }

    public void run() {
        context = WarehouseContext.instance();
        JFrame frame = context.getFrame();
        frame.getContentPane().removeAll();
        frame.getContentPane().setLayout(new BorderLayout(10, 10));
        frame.getContentPane().add(
                new JLabel("Warehouse login", SwingConstants.CENTER),
                BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 8, 8));
        buttons.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        buttons.add(new LoginClientButton());
        buttons.add(new LoginClerkButton());
        buttons.add(new LoginManagerButton());
        buttons.add(new LoginExitButton());
        frame.getContentPane().add(buttons, BorderLayout.CENTER);
        frame.setTitle("Warehouse - Login");
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
        frame.toFront();
    }
}

