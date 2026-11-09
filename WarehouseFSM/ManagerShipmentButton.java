import javax.swing.*;
import java.awt.event.*;
 
//ManagerMenu receive shipment
public class ManagerShipmentButton extends JButton implements ActionListener {
    public ManagerShipmentButton() {
        super("Receive a shipment");
        addActionListener(this);
    }
    public void actionPerformed(ActionEvent event) {
        ManagerMenuState.instance().receiveShipment();
    }
}