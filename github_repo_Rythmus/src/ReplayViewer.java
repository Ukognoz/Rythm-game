import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplayViewer extends JPanel implements ActionListener {
    private final int width;
    private final int height;
    private final List<FrameData> frames;
    private final int level;
    private final Timer timer;
    private int frameIndex = 0;
    private long replayStartTime;

    private final Color bgColor = new Color(24, 24, 32);
    private final Color accentColor = new Color(255, 85, 85);
    private final Color goldColor = new Color(241, 196, 15);
    private final Font fontUI = new Font("Segoe UI", Font.BOLD, 16);

    private static class FrameData {
        int t;
        int x;
        int y;

        FrameData(int t, int x, int y) {
            this.t = t;
            this.x = x;
            this.y = y;
        }
    }

    public ReplayViewer(int width, int height, String replayJson) {
        this.width = width;
        this.height = height;
        this.level = parseInt(replayJson, "level", 1);
        this.frames = parseFrames(replayJson);
        setBackground(bgColor);
        setFocusable(true);
        timer = new Timer(16, this);
        replayStartTime = System.currentTimeMillis();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    timer.stop();
                    launcher.showLobby();
                }
            }
        });
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!frames.isEmpty()) {
            int replayTime = (int)(System.currentTimeMillis() - replayStartTime);
            int lastFrameTime = frames.get(frames.size() - 1).t;
            if (lastFrameTime > 0 && replayTime > lastFrameTime) {
                replayStartTime = System.currentTimeMillis();
                replayTime = 0;
                frameIndex = 0;
            }
            while (frameIndex + 1 < frames.size() && frames.get(frameIndex + 1).t <= replayTime) {
                frameIndex++;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(fontUI);
        g2.setColor(Color.WHITE);
        g2.drawString("REPLAY", 20, 30);
        g2.drawString("ESC = Zurueck", width - 140, 30);
        g2.drawString("Level: " + level, width - 140, 58);

        drawPlatforms(g2);

        if (frames.isEmpty()) {
            g2.drawString("Keine Replay-Frames gefunden.", 20, 70);
            return;
        }

        FrameData frame = frames.get(frameIndex);
        g2.setColor(accentColor);
        g2.fillRect(frame.x, frame.y, 16, 16);

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Frame: " + (frameIndex + 1) + "/" + frames.size() + "  Zeit: " + frame.t + "ms", 20, 58);
    }

    private void drawPlatforms(Graphics2D g2) {
        int[][] platforms = getPlatformData(level);
        double scaleX = width / 800.0;
        double scaleY = height / 600.0;

        for (int[] platform : platforms) {
            int x = platform[0];
            int y = platform[1];
            int w = platform[2] * 16;
            int h = platform[3] * 16;
            boolean isGoal = platform[4] == 1;
            boolean isFloor = platform[5] == 1;

            int drawX;
            int drawY;
            int drawW;
            int drawH;
            if (isFloor) {
                drawX = 0;
                drawH = (int)(h * scaleY);
                drawY = height - drawH;
                drawW = width;
            } else {
                drawX = (int)(x * scaleX);
                drawY = (int)(y * scaleY);
                drawW = (int)(w * scaleX);
                drawH = (int)(h * scaleY);
            }

            g2.setColor(isGoal ? goldColor : new Color(100, 100, 120));
            g2.fillRect(drawX, drawY, drawW, drawH);
            if (isGoal) {
                g2.setColor(accentColor);
                g2.fillRect(drawX + drawW / 2, drawY - 16, 4, 16);
                g2.fillRect(drawX + drawW / 2 + 4, drawY - 16, 12, 8);
            }
        }
    }

    private int[][] getPlatformData(int replayLevel) {
        if (replayLevel == 1) {
            return new int[][]{
                    {0, 520, 50, 5, 0, 1},
                    {200, 420, 25, 1, 0, 0},
                    {400, 300, 20, 1, 0, 0},
                    {150, 180, 20, 1, 0, 0},
                    {350, 100, 12, 1, 1, 0}
            };
        }
        if (replayLevel == 2) {
            return new int[][]{
                    {0, 520, 50, 5, 0, 1},
                    {100, 430, 12, 1, 0, 0},
                    {350, 350, 12, 1, 0, 0},
                    {600, 260, 10, 1, 0, 0},
                    {300, 180, 14, 1, 0, 0},
                    {100, 100, 8, 1, 1, 0}
            };
        }
        return new int[][]{
                {0, 520, 50, 5, 0, 1},
                {250, 400, 16, 1, 0, 0},
                {480, 310, 12, 1, 0, 0},
                {120, 200, 14, 1, 0, 0},
                {400, 110, 8, 1, 1, 0}
        };
    }

    private static List<FrameData> parseFrames(String replayJson) {
        List<FrameData> parsedFrames = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\s*\"t\"\\s*:\\s*([0-9]+)\\s*,\\s*\"x\"\\s*:\\s*(-?[0-9]+)\\s*,\\s*\"y\"\\s*:\\s*(-?[0-9]+)\\s*\\}");
        Matcher matcher = pattern.matcher(replayJson);
        while (matcher.find()) {
            parsedFrames.add(new FrameData(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))
            ));
        }
        return parsedFrames;
    }

    private static int parseInt(String json, String key, int fallback) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }
}
