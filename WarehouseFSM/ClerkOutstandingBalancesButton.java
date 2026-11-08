import javax.swing.*;
import java.awt.event.*;

public class ClerkOutstandingBalancesButton extends JButton
        implements ActionListener {

    public ClerkOutstandingBalancesButton() {
        super("Outstanding Balances");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        ClerkMenuState.instance()
            .showClientsWithOutstandingBalance();
    }
}