import javax.swing.*;
import java.awt.event.*;

/** Button for the wishlist screen: changes the quantity of a wishlist item. */
public class WishlistChangeButton extends JButton implements ActionListener {
  public WishlistChangeButton() {
    super("Change a quantity");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    WishlistState.instance().changeQuantity();
  }
}
