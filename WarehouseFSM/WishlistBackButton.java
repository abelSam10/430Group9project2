import javax.swing.*;
import java.awt.event.*;

/** Button for the wishlist screen: returns to the client menu. */
public class WishlistBackButton extends JButton implements ActionListener {
  public WishlistBackButton() {
    super("Back to client menu");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    WishlistState.instance().backToClientMenu();
  }
}
