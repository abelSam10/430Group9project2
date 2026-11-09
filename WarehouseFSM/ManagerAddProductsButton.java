import javax.swing.*;
import java.awt.event.*;
 
// ManagerMenu add products to inventory 
public class ManagerAddProductsButton extends JButton implements ActionListener {
    public ManagerAddProductsButton() {
        super("Add products");
        addActionListener(this);
    }
    public void actionPerformed(ActionEvent event) {
        ManagerMenuState.instance().addProducts();
    }
}