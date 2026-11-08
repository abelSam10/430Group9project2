import javax.swing.*;
import java.awt.event.*;

public class ClerkLogoutButton extends JButton
        implements ActionListener {

    public ClerkLogoutButton() {
        super("Logout");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        ClerkMenuState.instance().logout();
    }
}