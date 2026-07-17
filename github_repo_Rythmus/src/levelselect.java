import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class levelselect extends JPanel implements MouseListener {
    private final int width;
    private final int height;

    private Rectangle lvl1Btn, lvl2Btn, lvl3Btn, backBtn;
    private int hoveredButton = 0;

    private final Color bgColor = new Color(24, 24, 32);
    private final Color accentColor = new Color(255, 85, 85);
    private final Color buttonColor = new Color(52, 152, 219);
    private final Color buttonHoverColor = new Color(41, 128, 185);
    private final Color lockedColor = new Color(50, 50, 60);

    private final Font fontTitle = new Font("Segoe UI", Font.BOLD, 40);
    private final Font fontUI = new Font("Consolas", Font.BOLD, 18);
    private final Font fontTime = new Font("Consolas", Font.ITALIC, 13);

    public levelselect(int width, int height) {
        this.width = width;
        this.height = height;

        int centerX = width / 2;
        int centerY = height / 2;

        lvl1Btn = new Rectangle(centerX - 120, centerY - 100, 240, 50);
        lvl2Btn = new Rectangle(centerX - 120, centerY - 10, 240, 50);
        lvl3Btn = new Rectangle(centerX - 120, centerY + 80, 240, 50);
        backBtn = new Rectangle(centerX - 120, centerY + 170, 240, 45);

        setBackground(bgColor);
        addMouseListener(this);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                if (lvl1Btn.contains(p)) hoveredButton = 1;
                else if (lvl2Btn.contains(p) && isLevelAvailable(2)) hoveredButton = 2;
                else if (lvl3Btn.contains(p) && isLevelAvailable(3)) hoveredButton = 3;
                else if (backBtn.contains(p)) hoveredButton = 4;
                else hoveredButton = 0;
                repaint();
            }
        });
    }

    private boolean isLevelAvailable(int level) {
        return launcher.unlockedLevel >= level || AuthService.isAdmin();
    }

    private String formatBestTime(double seconds) {
        if (seconds < 0) return launcher.language == 0 ? "Keine Zeit" : "No Time";
        return String.format("%.2fs", seconds);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(fontTitle);
        g2.setColor(accentColor);
        String title = launcher.language == 0 ? "LEVEL AUSWAHL" : "SELECT LEVEL";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (width - fm.stringWidth(title)) / 2, height / 2 - 150);

        g2.setFont(fontUI);
        FontMetrics fmBtn = g2.getFontMetrics();

        drawLevelButton(g2, lvl1Btn, "LEVEL 1", launcher.bestTimes[0], true, hoveredButton == 1, fmBtn);
        drawLevelButton(g2, lvl2Btn, "LEVEL 2", launcher.bestTimes[1], isLevelAvailable(2), hoveredButton == 2, fmBtn);
        drawLevelButton(g2, lvl3Btn, "LEVEL 3", launcher.bestTimes[2], isLevelAvailable(3), hoveredButton == 3, fmBtn);

        g2.setFont(fontUI);
        g2.setColor(hoveredButton == 4 ? new Color(127, 140, 141) : new Color(149, 165, 166));
        g2.fillRoundRect(backBtn.x, backBtn.y, backBtn.width, backBtn.height, 10, 10);
        g2.setColor(Color.WHITE);
        String backText = launcher.language == 0 ? "ZUR\u00DCCK" : "BACK";
        g2.drawString(backText, backBtn.x + (backBtn.width - fmBtn.stringWidth(backText)) / 2, backBtn.y + 28);
    }

    private void drawLevelButton(Graphics2D g2, Rectangle button, String text, double bestTime, boolean available, boolean hovered, FontMetrics fmBtn) {
        g2.setFont(fontUI);
        g2.setColor(available ? (hovered ? buttonHoverColor : buttonColor) : lockedColor);
        g2.fillRoundRect(button.x, button.y, button.width, button.height, 10, 10);
        g2.setColor(available ? Color.WHITE : Color.DARK_GRAY);
        String levelText = available ? text : "LOCKED " + text;
        g2.drawString(levelText, button.x + (button.width - fmBtn.stringWidth(levelText)) / 2, button.y + 32);

        g2.setFont(fontTime);
        g2.setColor(Color.GRAY);
        String bestText = (launcher.language == 0 ? "Bestzeit: " : "Best: ") + formatBestTime(bestTime);
        g2.drawString(bestText, button.x + (button.width - g2.getFontMetrics().stringWidth(bestText)) / 2, button.y + 65);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        if (lvl1Btn.contains(p)) startSelectedLevel(1);
        else if (lvl2Btn.contains(p) && isLevelAvailable(2)) startSelectedLevel(2);
        else if (lvl3Btn.contains(p) && isLevelAvailable(3)) startSelectedLevel(3);
        else if (backBtn.contains(p)) launcher.showLobby();
    }

    private void startSelectedLevel(int levelNumber) {
        game gamePanel = new game(width, height, levelNumber);
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (topFrame != null) {
            topFrame.getContentPane().removeAll();
            topFrame.getContentPane().add(gamePanel);
            topFrame.getContentPane().revalidate();
            topFrame.getContentPane().repaint();
            gamePanel.requestFocusInWindow();
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
