import javax.swing.*;
import java.awt.event.*;

public class ClerkShowClientsButton extends JButton
        implements ActionListener {

    public ClerkShowClientsButton() {
        super("Show Clients");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        ClerkMenuState.instance().showClients();
    }
}