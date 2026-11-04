import javax.swing.*;
import java.awt.event.*;

/** Button for the client menu: adds products to the active client's wishlist. */
public class ClientAddWishlistButton extends JButton implements ActionListener {
  public ClientAddWishlistButton() {
    super("Add to wishlist");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    ClientMenuState.instance().addToWishlist();
  }
}
