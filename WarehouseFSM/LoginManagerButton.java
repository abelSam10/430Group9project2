import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class LoginManagerButton extends JButton implements ActionListener {
    public LoginManagerButton() {
        super("Manager");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        LoginState.instance().loginManager();
    }
}
