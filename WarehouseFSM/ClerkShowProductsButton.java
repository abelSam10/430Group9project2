import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class ClerkShowProductsButton extends JButton
        implements ActionListener {

    public ClerkShowProductsButton() {
        super("Show Products");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        ClerkMenuState.instance().showProducts();
    }
}