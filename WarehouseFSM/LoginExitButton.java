import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class LoginExitButton extends JButton implements ActionListener {
    public LoginExitButton() {
        super("Exit");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        LoginState.instance().exit();
    }
}
