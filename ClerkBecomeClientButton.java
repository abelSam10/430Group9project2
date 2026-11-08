import javax.swing.*;
import java.awt.event.*;

public class ClerkBecomeClientButton extends JButton
        implements ActionListener {

    public ClerkBecomeClientButton() {
        super("Become Client");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        ClerkMenuState.instance().becomeClient();
    }
}