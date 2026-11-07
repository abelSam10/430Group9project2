import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class LoginClerkButton extends JButton implements ActionListener {
    public LoginClerkButton() {
        super("Clerk");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        LoginState.instance().loginClerk();
    }
}
