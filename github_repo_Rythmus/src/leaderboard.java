import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class leaderboard extends JPanel implements MouseListener {
    private int width, height, activeLevel = 1;
    private Rectangle backBtn;
    private Rectangle[] levelBtns = new Rectangle[3];
    private List<String[]> scoreData;
    private boolean isLoading = false;

    private final Color bgColor = new Color(24, 24, 32);
    private final Color accentColor = new Color(255, 85, 85);
    private final Color panelBg = new Color(32, 32, 42);

    public leaderboard(int width, int height) {
        this.width = width;
        this.height = height;
        this.backBtn = new Rectangle(width / 2 - 100, height - 70, 200, 40);
        for (int i = 0; i < 3; i++) {
            levelBtns[i] = new Rectangle(width / 2 - 150 + (i * 110), 100, 90, 40);
        }

        setBackground(bgColor);
        addMouseListener(this);
        loadData();
    }

    private void loadData() {
        isLoading = true;
        scoreData = null;
        repaint();

        new Thread(() -> {
            scoreData = AuthService.getLeaderboardData(activeLevel);
            isLoading = false;
            SwingUtilities.invokeLater(this::repaint);
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(accentColor);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
        String title = "LEADERBOARD - LEVEL " + activeLevel;
        drawCentered(g2, title, width / 2, 60);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        for (int i = 0; i < 3; i++) {
            g2.setColor(activeLevel == i + 1 ? accentColor : panelBg);
            g2.fillRoundRect(levelBtns[i].x, levelBtns[i].y, levelBtns[i].width, levelBtns[i].height, 10, 10);
            g2.setColor(Color.WHITE);
            drawCentered(g2, "L" + (i + 1), levelBtns[i].x + levelBtns[i].width / 2, levelBtns[i].y + 26);
        }

        g2.setColor(panelBg);
        g2.fillRoundRect(width / 2 - 200, 160, 400, 300, 15, 15);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));

        if (isLoading) {
            g2.setColor(Color.LIGHT_GRAY);
            drawCentered(g2, "Lade Zeiten...", width / 2, 310);
        } else if (scoreData == null || scoreData.isEmpty()) {
            g2.setColor(Color.LIGHT_GRAY);
            drawCentered(g2, "Noch keine Zeiten", width / 2, 310);
        } else {
            g2.setColor(Color.WHITE);
            for (int i = 0; i < Math.min(scoreData.size(), 8); i++) {
                int rowY = 200 + (i * 35);
                g2.drawString((i + 1) + ". " + scoreData.get(i)[0], width / 2 - 170, rowY);
                String time = String.format("%.2fs", Integer.parseInt(scoreData.get(i)[1]) / 100.0);
                g2.drawString(time, width / 2 + 100, rowY);
            }
        }

        g2.setColor(Color.GRAY);
        g2.fillRoundRect(backBtn.x, backBtn.y, backBtn.width, backBtn.height, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        drawCentered(g2, launcher.language == 0 ? "ZUR\u00DCCK" : "BACK", backBtn.x + backBtn.width / 2, backBtn.y + 26);
    }

    private void drawCentered(Graphics2D g2, String text, int centerX, int baselineY) {
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    public void mousePressed(MouseEvent e) {
        for (int i = 0; i < 3; i++) {
            if (levelBtns[i].contains(e.getPoint())) {
                activeLevel = i + 1;
                loadData();
            }
        }
        if (backBtn.contains(e.getPoint())) {
            launcher.showLobby();
        }
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
