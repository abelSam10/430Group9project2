import javax.swing.*;
import java.awt.event.*;
 
// ManagerMenu display product waitlist
public class ManagerWaitlistButton extends JButton implements ActionListener {
    public ManagerWaitlistButton() {
        super("Display product's waitlist");
        addActionListener(this);
    }
    public void actionPerformed(ActionEvent event) {
        ManagerMenuState.instance().displayWaitlist();
    }
}