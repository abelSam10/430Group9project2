import javax.swing.*;
import java.awt.event.*;

/** Button for the client menu screen: opens the wishlist operations screen. */
public class ClientWishlistOpsButton extends JButton implements ActionListener {
  public ClientWishlistOpsButton() {
    super("Perform wishlist operations");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    ClientMenuState.instance().wishlistOperations();
  }
}
