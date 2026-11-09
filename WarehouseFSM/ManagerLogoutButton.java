import javax.swing.*;
import java.awt.event.*;
 
//ManagerMenu logout
public class ManagerLogoutButton extends JButton implements ActionListener {
    public ManagerLogoutButton() {
        super("Logout");
        addActionListener(this);
    }
    public void actionPerformed(ActionEvent event) {
        ManagerMenuState.instance().logout();
    }
}
