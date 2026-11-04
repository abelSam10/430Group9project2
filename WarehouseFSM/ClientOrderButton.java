import javax.swing.*;
import java.awt.event.*;

/** Button for the client menu: orders everything in the active client's wishlist. */
public class ClientOrderButton extends JButton implements ActionListener {
  public ClientOrderButton() {
    super("Place an order");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    ClientMenuState.instance().placeOrder();
  }
}
