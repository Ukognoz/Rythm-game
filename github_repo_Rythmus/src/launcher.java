import javax.swing.*;
import java.awt.*;

public class launcher {
    public static JFrame window;
    public static int currentWidth = 800;
    public static int currentHeight = 600;

    public static int language = 1;      // 0 = Deutsch, 1 = English
    public static int displayMode = 0;   // 0 = Fenster, 1 = Vollbild, 2 = Rahmenlos
    public static int unlockedLevel = 1;
    public static double[] bestTimes = {-1.0, -1.0, -1.0};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SaveSystem.loadSettings();
            window = new JFrame("Rhythm Jumper");
            window.setSize(currentWidth, currentHeight);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLocationRelativeTo(null);
            window.setResizable(false);

            if (AuthService.checkAutomaticLogin()) {
                loadAccountProgress();
                showLobby();
            } else {
                window.setVisible(true);
                LoginWindow.showLogin(() -> {
                    loadAccountProgress();
                    showLobby();
                });
            }
        });
    }

    public static void loadAccountProgress() {
        SaveSystem.loadGame();
        boolean loadedFromBackend = AuthService.loadProgressFromBackend();
        if (loadedFromBackend) {
            SaveSystem.saveGame();
        } else {
            AuthService.saveProgressToBackend();
        }
        if (AuthService.isAdmin()) {
            unlockedLevel = 3;
            SaveSystem.saveGame();
        }
    }

    public static void showLobby() {
        if (window == null) return;

        // Verhindert den Text-Bug: Löscht alle alten Reste aus dem Fenster
        window.getContentPane().removeAll();

        JPanel lobbyPanel = new JPanel();
        lobbyPanel.setLayout(new BoxLayout(lobbyPanel, BoxLayout.Y_AXIS));
        lobbyPanel.setBackground(new Color(24, 24, 32));
        lobbyPanel.setBorder(BorderFactory.createEmptyBorder(60, 50, 50, 50));

        String title = (language == 0) ? "RHYTHMUS SPRINGER" : "RHYTHM JUMPER";
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
        titleLabel.setForeground(new Color(255, 85, 85));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lobbyPanel.add(titleLabel);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        JButton playBtn = createStyledButton(language == 0 ? "SPIEL STARTEN" : "START GAME", new Color(46, 204, 113));
        JButton lbBtn = createStyledButton(language == 0 ? "LEADERBOARD" : "LEADERBOARD", new Color(241, 196, 15));
        JButton replaysBtn = createStyledButton("REPLAYS", new Color(52, 152, 219));
        JButton settingsBtn = createStyledButton(language == 0 ? "EINSTELLUNGEN" : "SETTINGS", new Color(142, 68, 173));
        JButton logoutBtn = createStyledButton(language == 0 ? "ABMELDEN" : "LOGOUT", new Color(231, 76, 60));

        lobbyPanel.add(playBtn);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        lobbyPanel.add(lbBtn);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        lobbyPanel.add(replaysBtn);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        lobbyPanel.add(settingsBtn);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        lobbyPanel.add(logoutBtn);

        playBtn.addActionListener(event -> {
            window.getContentPane().removeAll();
            window.add(new levelselect(currentWidth, currentHeight));
            window.revalidate();
            window.repaint();
        });

        lbBtn.addActionListener(event -> {
            window.getContentPane().removeAll();
            window.add(new leaderboard(currentWidth, currentHeight));
            window.revalidate();
            window.repaint();
        });

        replaysBtn.addActionListener(event -> {
            window.getContentPane().removeAll();
            window.add(new ReplayLibrary(currentWidth, currentHeight));
            window.revalidate();
            window.repaint();
        });

        settingsBtn.addActionListener(event -> {
            window.getContentPane().removeAll();
            window.add(new settings(currentWidth, currentHeight));
            window.revalidate();
            window.repaint();
        });

        logoutBtn.addActionListener(event -> {
            AuthService.logout();
            window.getContentPane().removeAll();
            window.revalidate();
            window.repaint();
            LoginWindow.showLogin(() -> {
                loadAccountProgress();
                showLobby();
            });
        });

        window.add(lobbyPanel);
        window.revalidate();
        window.repaint();

        if (!window.isVisible()) {
            window.setVisible(true);
        }
    }

    public static void updateDisplayMode() {
        if (window == null) return;
        window.dispose();

        if (displayMode == 0) {
            window.setUndecorated(false);
            window.setExtendedState(JFrame.NORMAL);
            window.setSize(currentWidth, currentHeight);
        } else if (displayMode == 1) {
            window.setUndecorated(true);
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            window.setUndecorated(true);
            window.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        }
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private static JButton createStyledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(240, 45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Consolas", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
