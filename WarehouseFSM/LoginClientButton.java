import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class LoginClientButton extends JButton implements ActionListener {
    public LoginClientButton() {
        super("Client");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        LoginState.instance().loginClient();
    }
}
