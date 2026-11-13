import javax.swing.*;
import java.awt.event.*;

/** Button for the wishlist screen: removes a product from the wishlist. */
public class WishlistRemoveButton extends JButton implements ActionListener {
  public WishlistRemoveButton() {
    super("Remove an item");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    WishlistState.instance().removeFromWishlist();
  }
}
