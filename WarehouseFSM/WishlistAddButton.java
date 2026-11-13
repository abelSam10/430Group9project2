import javax.swing.*;
import java.awt.event.*;

/** Button for the wishlist screen: adds products to the wishlist. */
public class WishlistAddButton extends JButton implements ActionListener {
  public WishlistAddButton() {
    super("Add items");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    WishlistState.instance().addToWishlist();
  }
}
