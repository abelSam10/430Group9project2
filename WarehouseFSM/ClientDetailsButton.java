import javax.swing.*;
import java.awt.event.*;

/** Button for the client menu: shows the active client's details. */
public class ClientDetailsButton extends JButton implements ActionListener {
  public ClientDetailsButton() {
    super("Show client details");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    ClientMenuState.instance().showClientDetails();
  }
}
