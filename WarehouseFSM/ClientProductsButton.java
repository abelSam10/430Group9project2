import javax.swing.*;
import java.awt.event.*;

/** Button for the client menu: lists the products with their prices. */
public class ClientProductsButton extends JButton implements ActionListener {
  public ClientProductsButton() {
    super("Show products");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    ClientMenuState.instance().showProducts();
  }
}
