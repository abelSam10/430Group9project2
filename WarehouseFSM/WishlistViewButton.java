import javax.swing.*;
import java.awt.event.*;

/** Button for the wishlist screen: shows the active client's wishlist. */
public class WishlistViewButton extends JButton implements ActionListener {
  public WishlistViewButton() {
    super("View wishlist");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    WishlistState.instance().displayWishlist();
  }
}
