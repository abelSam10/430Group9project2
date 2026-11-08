import javax.swing.*;
import java.awt.event.*;

public class ClerkRecordPaymentButton extends JButton
        implements ActionListener {

    public ClerkRecordPaymentButton() {
        super("Record Payment");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        ClerkMenuState.instance().recordPayment();
    }
}