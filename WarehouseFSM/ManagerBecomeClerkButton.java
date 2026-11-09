import javax.swing.*;
import java.awt.event.*;
 
// Manager Menu become clerk
public class ManagerBecomeClerkButton extends JButton implements ActionListener {
    public ManagerBecomeClerkButton() {
        super("Become clerk");
        addActionListener(this);
    }
    public void actionPerformed(ActionEvent event) {
        ManagerMenuState.instance().becomeClerk();
    }
}