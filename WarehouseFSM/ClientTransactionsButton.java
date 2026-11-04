import javax.swing.*;
import java.awt.event.*;

/** Button for the client menu: shows the active client's orders and balance. */
public class ClientTransactionsButton extends JButton implements ActionListener {
  public ClientTransactionsButton() {
    super("Show transactions");
    addActionListener(this);
  }
  public void actionPerformed(ActionEvent event) {
    ClientMenuState.instance().showTransactions();
  }
}
