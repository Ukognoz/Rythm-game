import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class GameLoop {
    private int currentScore = 0;
    private long startTime;
    private long lastAutoSyncTime;

    // Spielfeld-Größe (passe die Werte an deine Fenstergröße an)
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    public void initGame() {
        startTime = System.currentTimeMillis();       // Spielzeit-Zähler startet
        lastAutoSyncTime = System.currentTimeMillis(); // Letzter Sync-Zeitstempel
    }

    // Diese Methode wird in deiner Schleife 60-mal pro Sekunde aufgerufen
    public void update() {
        long currentTime = System.currentTimeMillis();

        // AUTOMATISCHES ABSENDEN ALLE 60 SEKUNDEN
        if (currentTime - lastAutoSyncTime >= 60000) {
            System.out.println("[Cloud] 60 Sekunden abgelaufen. Automatischer Leaderboard-Sync...");
            AuthService.submitScore(currentScore);
            lastAutoSyncTime = currentTime; // Timer zurücksetzen
        }

        // ... Hier steht deine Bewegung der Spielfigur, Kollisionen etc. ...
    }

    public void onLevelComplete() {
        System.out.println("[Cloud] Level geschafft! Score wird synchronisiert...");
        AuthService.submitScore(currentScore);
    }

    // Berechnet die Playtime im Format HH:MM:SS
    public String getFormattedPlaytime() {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        long totalSeconds = elapsedMillis / 1000;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // DAS BEHEBT DEN TEXT-BUG:
    public void draw(Graphics2D g) {
        // 1. WICHTIG: Den gesamten Bildschirm mit der Hintergrundfarbe löschen!
        // Ohne das stapeln sich alte Frames und Texte übereinander!
        g.setColor(new Color(24, 24, 32)); // Gleiches Dunkelgrau wie im Launcher
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 2. Jetzt erst die UI-Texte frisch auf die leere Fläche zeichnen
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));

        // Playtime oben rechts zeichnen (z.B. bei X=650, Y=30)
        g.drawString("⏱ Spielzeit: " + getFormattedPlaytime(), WIDTH - 150, 30);

        // Score oben links zeichnen
        g.drawString("🏆 Score: " + currentScore, 30, 30);

        // 3. ... Hier drunter zeichnest du deine Plattformen und die Spielfigur ...
    }

    // Hilfsmethode, falls du Punkte hinzufügen willst
    public void addScore(int points) {
        this.currentScore += points;
    }
}