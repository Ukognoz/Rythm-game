import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginWindow {
    private static final Color BG_COLOR = new Color(24, 24, 32);
    private static final Color PANEL_BG = new Color(32, 32, 42);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(255, 85, 85);
    private static final Color BUTTON_PRIMARY = new Color(46, 204, 113);
    private static final Color BUTTON_SECONDARY = new Color(142, 68, 173);

    public static void showLogin(Runnable onSuccess) {
        if (AuthService.checkAutomaticLogin()) {
            onSuccess.run();
            return;
        }

        JFrame frame = new JFrame(launcher.language == 0 ? "Rhythmus Springer - Login" : "Rhythm Jumper - Login");
        frame.setSize(400, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        String gameTitle = (launcher.language == 0) ? "RHYTHMUS SPRINGER" : "RHYTHM JUMPER";
        JLabel titleLabel = new JLabel(gameTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBackground(BG_COLOR);
        formPanel.setMaximumSize(new Dimension(340, 130));

        formPanel.add(createStyledLabel(launcher.language == 0 ? "Account Name (nur Reg):" : "Account Name (Reg only):"));
        JTextField nameField = createStyledTextField();
        formPanel.add(nameField);

        formPanel.add(createStyledLabel("E-Mail:"));
        JTextField emailField = createStyledTextField();
        formPanel.add(emailField);

        formPanel.add(createStyledLabel(launcher.language == 0 ? "Passwort:" : "Password:"));
        JPasswordField passField = createStyledPasswordField();
        formPanel.add(passField);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton forgotBtn = new JButton(launcher.language == 0 ? "Passwort vergessen?" : "Forgot password?");
        forgotBtn.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        forgotBtn.setForeground(Color.LIGHT_GRAY);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(forgotBtn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        actionPanel.setBackground(BG_COLOR);
        actionPanel.setMaximumSize(new Dimension(340, 45));

        JButton loginBtn = createModernButton(launcher.language == 0 ? "Einloggen" : "Login", BUTTON_PRIMARY);
        JButton regBtn = createModernButton(launcher.language == 0 ? "Registrieren" : "Register", BUTTON_SECONDARY);
        actionPanel.add(loginBtn);
        actionPanel.add(regBtn);
        mainPanel.add(actionPanel);

        frame.add(mainPanel);
        frame.setVisible(true);

        loginBtn.addActionListener(event -> {
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            if (AuthService.loginUser(email, pass)) {
                frame.dispose();
                onSuccess.run();
            } else {
                JOptionPane.showMessageDialog(frame, launcher.language == 0 ? "Login fehlgeschlagen!" : "Login failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        regBtn.addActionListener(event -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) return;
            if (AuthService.registerUser(email, pass, name)) {
                JOptionPane.showMessageDialog(frame, launcher.language == 0 ? "Verifizierungs-Link gesendet!" : "Verification link sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        forgotBtn.addActionListener(event -> {
            String email = JOptionPane.showInputDialog(frame, launcher.language == 0 ? "E-Mail eingeben:" : "Enter E-Mail:");
            if (email != null && !email.trim().isEmpty()) {
                if (AuthService.sendPasswordReset(email.trim())) {
                    JOptionPane.showMessageDialog(frame, launcher.language == 0 ? "Link gesendet!" : "Link sent!");
                }
            }
        });
    }

    private static JLabel createStyledLabel(String text) {
        JLabel l = new JLabel(text); l.setForeground(TEXT_COLOR);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12)); return l;
    }

    private static JTextField createStyledTextField() {
        JTextField tf = new JTextField(); tf.setBackground(PANEL_BG); tf.setForeground(TEXT_COLOR);
        tf.setCaretColor(TEXT_COLOR); tf.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 75))); return tf;
    }

    private static JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField(); pf.setBackground(PANEL_BG); pf.setForeground(TEXT_COLOR);
        pf.setCaretColor(TEXT_COLOR); pf.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 75))); return pf;
    }

    private static JButton createModernButton(String text, Color c) {
        JButton b = new JButton(text); b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13)); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(c.brighter()); }
            public void mouseExited(MouseEvent e) { b.setBackground(c); }
        });
        return b;
    }
}
