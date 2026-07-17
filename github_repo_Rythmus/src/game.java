import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class game extends JPanel implements ActionListener, KeyListener {

    private final int screenWidth;
    private final int screenHeight;
    private final Timer gameTimer;
    private final int currentLevel;

    private final double scaleX;
    private final double scaleY;

    private final Color bgColor = new Color(24, 24, 32);
    private final Color accentColor = new Color(255, 85, 85);
    private final Color buttonColor = new Color(46, 204, 113);
    private final Color goldColor = new Color(241, 196, 15);
    private final Font fontUI = new Font("Consolas", Font.BOLD, 20);
    private final Font fontWin = new Font("Segoe UI", Font.BOLD, 40);

    private final long startTime;
    private double finalElapsedTime = 0.0;

    private double playerX;
    private double playerY;
    private final int playerWidth = 16;
    private final int playerHeight = 16;

    private double velX;
    private double velY = 0.0;
    private final double gravity;
    private final double jumpStrength;

    private boolean isGrounded = false;
    private boolean facingRight = true;
    private boolean isGameWon = false;
    private int replayTick = 0;
    private final StringBuilder replayFrames = new StringBuilder();

    public static class Platform {
        int baseX, baseY, baseW, baseH;
        Rectangle scaledRect;
        boolean isGoal;
        boolean isFloor;

        public Platform(int x, int y, int widthTiles, int heightTiles, boolean isGoal, boolean isFloor) {
            this.baseX = x;
            this.baseY = y;
            this.baseW = widthTiles * 16;
            this.baseH = heightTiles * 16;
            this.isGoal = isGoal;
            this.isFloor = isFloor;
            this.scaledRect = new Rectangle(x, y, baseW, baseH);
        }

        public void scale(double scaleX, double scaleY, int currentScreenWidth, int currentScreenHeight) {
            if (isFloor) {
                int floorHeight = (int)(baseH * scaleY);
                this.scaledRect.x = 0;
                this.scaledRect.y = currentScreenHeight - floorHeight;
                this.scaledRect.width = currentScreenWidth;
                this.scaledRect.height = floorHeight;
            } else {
                this.scaledRect.x = (int) (baseX * scaleX);
                this.scaledRect.y = (int) (baseY * scaleY);
                this.scaledRect.width = (int) (baseW * scaleX);
                this.scaledRect.height = (int) (baseH * scaleY);
            }
        }
    }

    private Platform[] platforms;

    public game(int width, int height, int selectedLevel) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.currentLevel = selectedLevel;
        this.startTime = System.currentTimeMillis();

        this.scaleX = (double) width / 800.0;
        this.scaleY = (double) height / 600.0;

        double speedMultiplier = 1.0;

        // Alle Level nutzen nun die vollen 800x600 Basis-Koordinaten und skalieren sauber mit!
        if (selectedLevel == 1) {
            platforms = new Platform[]{
                    new Platform(0, 520, 50, 5, false, true),
                    new Platform(200, 420, 25, 1, false, false),
                    new Platform(400, 300, 20, 1, false, false),
                    new Platform(150, 180, 20, 1, false, false),
                    new Platform(350, 100, 12, 1, true, false)
            };
            speedMultiplier = 1.0;
        } else if (selectedLevel == 2) {
            platforms = new Platform[]{
                    new Platform(0, 520, 50, 5, false, true),
                    new Platform(100, 430, 12, 1, false, false),
                    // Überarbeitete Koordinaten, damit sich Level 2 über den ganzen Bildschirm erstreckt:
                    new Platform(350, 350, 12, 1, false, false),
                    new Platform(600, 260, 10, 1, false, false),
                    new Platform(300, 180, 14, 1, false, false),
                    new Platform(100, 100, 8, 1, true, false)
            };
            speedMultiplier = 1.35;
        } else {
            platforms = new Platform[]{
                    new Platform(0, 520, 50, 5, false, true),
                    new Platform(250, 400, 16, 1, false, false),
                    new Platform(480, 310, 12, 1, false, false),
                    new Platform(120, 200, 14, 1, false, false),
                    new Platform(400, 110, 8, 1, true, false)
            };
            speedMultiplier = 1.15;
        }

        for (Platform plat : platforms) {
            plat.scale(scaleX, scaleY, screenWidth, screenHeight);
        }

        this.playerX = 100 * scaleX;
        this.playerY = platforms[0].scaledRect.y - playerHeight - 20;

        this.velX = 4.0 * scaleX * speedMultiplier;
        this.gravity = 0.6 * scaleY;
        this.jumpStrength = -13.0 * scaleY;

        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(bgColor);
        setFocusable(true);
        addKeyListener(this);

        gameTimer = new Timer(16, this);
        gameTimer.start();

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing()) {
                if (gameTimer != null && gameTimer.isRunning()) {
                    gameTimer.stop();
                    System.out.println("Old game timer successfully killed to prevent text overlay bug.");
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameWon) {
            updatePhysics();
        }
        repaint();
    }

    private void updatePhysics() {
        recordReplayFrame();
        playerX += velX;

        if (playerX + playerWidth >= screenWidth) {
            playerX = screenWidth - playerWidth;
            velX = -Math.abs(velX);
            facingRight = false;
        } else if (playerX <= 0) {
            playerX = 0;
            velX = Math.abs(velX);
            facingRight = true;
        }

        Rectangle playerRect = new Rectangle((int)playerX, (int)playerY, playerWidth, playerHeight);
        for (Platform plat : platforms) {
            if (playerRect.intersects(plat.scaledRect)) {
                if (!(playerY + playerHeight - velY <= plat.scaledRect.y + 6)) {
                    if (velX > 0) {
                        playerX = plat.scaledRect.x - playerWidth;
                        velX = -Math.abs(velX);
                        facingRight = false;
                    } else if (velX < 0) {
                        playerX = plat.scaledRect.x + plat.scaledRect.width;
                        velX = Math.abs(velX);
                        facingRight = true;
                    }
                    playerRect.x = (int)playerX;
                }
            }
        }

        velY += gravity;
        playerY += velY;

        isGrounded = false;
        int currentPlatformIdx = -1;

        playerRect = new Rectangle((int)playerX, (int)Math.ceil(playerY), playerWidth, playerHeight);

        for (int i = 0; i < platforms.length; i++) {
            Platform plat = platforms[i];
            if (playerRect.intersects(plat.scaledRect)) {
                if (velY > 0) {
                    if (playerY + playerHeight - velY <= plat.scaledRect.y + 6) {
                        playerY = plat.scaledRect.y - playerHeight;
                        velY = 0;
                        isGrounded = true;
                        currentPlatformIdx = i;
                    }
                } else if (velY < 0) {
                    if (playerY - velY >= plat.scaledRect.y + plat.scaledRect.height - 6) {
                        playerY = plat.scaledRect.y + plat.scaledRect.height;
                        velY = 0;
                    }
                }
            }
        }

        if (isGrounded && currentPlatformIdx != -1) {
            Platform plat = platforms[currentPlatformIdx];

            if (plat.isGoal) {
                isGameWon = true;
                gameTimer.stop();

                finalElapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;

                if (currentLevel == launcher.unlockedLevel && launcher.unlockedLevel < 3) {
                    launcher.unlockedLevel++;
                }

                int arrayIndex = currentLevel - 1;
                boolean isNewBestTime = launcher.bestTimes[arrayIndex] < 0 || finalElapsedTime < launcher.bestTimes[arrayIndex];
                if (isNewBestTime) {
                    launcher.bestTimes[arrayIndex] = finalElapsedTime;
                }

                if (isNewBestTime) {
                    int timeCentiseconds = (int)Math.round(finalElapsedTime * 100);
                    AuthService.submitScore(currentLevel, timeCentiseconds);
                }

                ReplayService.saveReplay(currentLevel, finalElapsedTime, replayFrames.toString());
                SaveSystem.saveGame();

                Timer returnTimer = new Timer(3000, event -> launcher.showLobby());
                returnTimer.setRepeats(false);
                returnTimer.start();
                return;
            }

            if (velX > 0 && (playerX + playerWidth >= plat.scaledRect.x + plat.scaledRect.width)) {
                playerX = plat.scaledRect.x + plat.scaledRect.width - playerWidth;
                velX = -Math.abs(velX);
                facingRight = false;
            } else if (velX < 0 && (playerX <= plat.scaledRect.x)) {
                playerX = plat.scaledRect.x;
                velX = Math.abs(velX);
                facingRight = true;
            }
        }
    }

    private void recordReplayFrame() {
        replayTick++;
        if (replayTick % 4 != 0) {
            return;
        }
        if (replayFrames.length() > 0) {
            replayFrames.append(",");
        }
        replayFrames.append("{\"t\":")
                .append(System.currentTimeMillis() - startTime)
                .append(",\"x\":")
                .append((int)playerX)
                .append(",\"y\":")
                .append((int)playerY)
                .append("}");
    }

    private void jump() {
        if (isGrounded) {
            velY = jumpStrength;
            isGrounded = false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(bgColor);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (Platform plat : platforms) {
            if (plat.isGoal) {
                g2.setColor(goldColor);
                g2.fillRect(plat.scaledRect.x, plat.scaledRect.y, plat.scaledRect.width, plat.scaledRect.height);
                g2.setColor(accentColor);
                g2.fillRect(plat.scaledRect.x + plat.scaledRect.width / 2, plat.scaledRect.y - 16, 4, 16);
                g2.fillRect(plat.scaledRect.x + plat.scaledRect.width / 2 + 4, plat.scaledRect.y - 16, 12, 8);
            } else {
                g2.setColor(new Color(100, 100, 120));
                g2.fillRect(plat.scaledRect.x, plat.scaledRect.y, plat.scaledRect.width, plat.scaledRect.height);
            }
        }

        g2.setColor(accentColor);
        g2.fillRect((int)playerX, (int)playerY, playerWidth, playerHeight);

        double currentElapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        String timeString = String.format("TIME: %.2fs", isGameWon ? finalElapsedTime : currentElapsed);

        String statusText = "";
        Color statusColor = Color.WHITE;

        if (isGrounded) {
            statusText = launcher.language == 0 ? "BEREIT ZUM SPRINGEN! (SPACE)" : "READY TO JUMP! (SPACE)";
            statusColor = buttonColor;
        } else {
            statusText = launcher.language == 0 ? "IN DER LUFT..." : "IN THE AIR...";
            statusColor = accentColor;
        }

        if (!isGameWon) {
            g2.setFont(fontUI);
            g2.setColor(statusColor);
            g2.drawString(statusText, 15, 30);

            g2.setColor(Color.WHITE);
            g2.drawString(timeString, 15, 60);

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("Level: " + currentLevel, screenWidth - 120, 30);
        }

        if (isGameWon) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setFont(fontWin);
            g2.setColor(goldColor);
            FontMetrics fm = g2.getFontMetrics();

            String winText = launcher.language == 0 ? "ZIEL ERREICHT!" : "GOAL REACHED!";
            g2.drawString(winText, (screenWidth - fm.stringWidth(winText)) / 2, screenHeight / 2 - 30);

            g2.setFont(fontUI);
            g2.setColor(Color.WHITE);
            String scoreText = (launcher.language == 0 ? "Deine Zeit: " : "Your Time: ") + timeString;
            g2.drawString(scoreText, (screenWidth - g2.getFontMetrics().stringWidth(scoreText)) / 2, screenHeight / 2 + 30);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            jump();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
