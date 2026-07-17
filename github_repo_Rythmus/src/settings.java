import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class settings extends JPanel implements MouseListener {

    private int width;
    private int height;

    private Rectangle modeBtn, langBtn, backBtn;
    private int hoveredButton = 0;

    private final Color bgColor = new Color(24, 24, 32);
    private final Color accentColor = new Color(255, 85, 85);
    private final Color buttonColor = new Color(142, 68, 173);
    private final Color buttonHoverColor = new Color(125, 60, 152);

    private final Font fontTitle = new Font("Segoe UI", Font.BOLD, 40);
    private final Font fontUI = new Font("Consolas", Font.BOLD, 18);

    public settings(int width, int height) {
        this.width = width;
        this.height = height;
        recalculateButtonPositions();
        setBackground(bgColor);
        addMouseListener(this);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                if (modeBtn.contains(p)) hoveredButton = 1;
                else if (langBtn.contains(p)) hoveredButton = 2;
                else if (backBtn.contains(p)) hoveredButton = 3;
                else hoveredButton = 0;
                repaint();
            }
        });
    }

    private void recalculateButtonPositions() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        modeBtn = new Rectangle(centerX - 150, centerY - 60, 300, 50);
        langBtn = new Rectangle(centerX - 150, centerY + 20, 300, 50);
        backBtn = new Rectangle(centerX - 150, centerY + 110, 300, 45);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(fontTitle);
        g2.setColor(accentColor);
        String title = launcher.language == 0 ? "EINSTELLUNGEN" : "SETTINGS";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (this.width - fm.stringWidth(title)) / 2, this.height / 2 - 120);

        g2.setFont(fontUI);
        FontMetrics fmBtn = g2.getFontMetrics();

        String modeText = "";
        if (launcher.displayMode == 0) modeText = launcher.language == 0 ? "MODUS: FENSTER" : "MODE: WINDOWED";
        else if (launcher.displayMode == 1) modeText = launcher.language == 0 ? "MODUS: VOLLBILD" : "MODE: FULLSCREEN";
        else modeText = launcher.language == 0 ? "MODUS: RAHMENLOS" : "MODE: BORDERLESS";

        g2.setColor(hoveredButton == 1 ? buttonHoverColor : buttonColor);
        g2.fillRoundRect(modeBtn.x, modeBtn.y, modeBtn.width, modeBtn.height, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawString(modeText, modeBtn.x + (modeBtn.width - fmBtn.stringWidth(modeText)) / 2, modeBtn.y + 32);

        String langText = launcher.language == 0 ? "SPRACHE: DEUTSCH" : "LANGUAGE: ENGLISH";
        g2.setColor(hoveredButton == 2 ? buttonHoverColor : buttonColor);
        g2.fillRoundRect(langBtn.x, langBtn.y, langBtn.width, langBtn.height, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawString(langText, langBtn.x + (langBtn.width - fmBtn.stringWidth(langText)) / 2, langBtn.y + 32);

        g2.setColor(hoveredButton == 3 ? new Color(127, 140, 141) : new Color(149, 165, 166));
        g2.fillRoundRect(backBtn.x, backBtn.y, backBtn.width, backBtn.height, 10, 10);
        g2.setColor(Color.WHITE);
        String backStr = launcher.language == 0 ? "ZUR\u00DCCK" : "BACK";
        g2.drawString(backStr, backBtn.x + (backBtn.width - fmBtn.stringWidth(backStr)) / 2, backBtn.y + 28);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        if (modeBtn.contains(p)) {
            launcher.displayMode = (launcher.displayMode + 1) % 3;
            launcher.updateDisplayMode();
            SaveSystem.saveGame();

            if (topFrame != null) {
                this.width = topFrame.getWidth();
                this.height = topFrame.getHeight();
                recalculateButtonPositions();
                topFrame.getContentPane().removeAll();
                topFrame.getContentPane().add(this);
                topFrame.getContentPane().revalidate();
                topFrame.getContentPane().repaint();
            }
        } else if (langBtn.contains(p)) {
            launcher.language = (launcher.language + 1) % 2;
            SaveSystem.saveGame();
            repaint();
        } else if (backBtn.contains(p)) {
            launcher.showLobby();
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}