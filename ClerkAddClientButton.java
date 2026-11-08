import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class ClerkAddClientButton extends JButton
        implements ActionListener {

    public ClerkAddClientButton() {
        super("Add Client");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        ClerkMenuState.instance().addClient();
    }
}