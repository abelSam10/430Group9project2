import javax.swing.*;
import java.awt.event.*;

/** Button for the client menu: logs out, and Context picks the next state. */
public class ClientLogoutButton extends JButton implements ActionListener {
  public ClientLogoutButton() {
    super("Logout");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    ClientMenuState.instance().logout();
  }
}
