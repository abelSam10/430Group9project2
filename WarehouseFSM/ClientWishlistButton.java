import javax.swing.*;
import java.awt.event.*;

/** Button for the client menu: shows the active client's wishlist. */
public class ClientWishlistButton extends JButton implements ActionListener {
  public ClientWishlistButton() {
    super("Display wishlist");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    ClientMenuState.instance().displayWishlist();
  }
}
