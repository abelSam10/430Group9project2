import javax.swing.*;
import java.awt.event.*;

/** Button for the wishlist screen: orders everything in the wishlist. */
public class WishlistOrderButton extends JButton implements ActionListener {
  public WishlistOrderButton() {
    super("Place an order");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    WishlistState.instance().placeOrder();
  }
}
