import java.io.*;
import java.util.Random;

public class SaveSystem {
    private static final String SAVE_FILE = "savegame.dat";
    private static final String SETTINGS_FILE = "settings.json";

    // --- SETTINGS AUTOMATISCH SPEICHERN (JSON) ---
    public static void saveSettings() {
        try (FileWriter fw = new FileWriter(SETTINGS_FILE)) {
            String json = "{\n" +
                    "  \"displayMode\": " + launcher.displayMode + ",\n" +
                    "  \"language\": " + launcher.language + "\n" +
                    "}";
            fw.write(json);
            System.out.println("Settings automatisch in settings.json gespeichert.");
        } catch (IOException e) {
            System.out.println("Fehler beim automatischen Speichern der Settings.");
        }
    }

    // --- SETTINGS LADEN (JSON) ---
    public static void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            // Wenn keine Datei da ist, direkt Standard-Settings erstellen und speichern
            saveSettings();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String content = sb.toString();

            launcher.displayMode = Integer.parseInt(fetchJsonValue(content, "displayMode"));
            launcher.language = Integer.parseInt(fetchJsonValue(content, "language"));
        } catch (Exception e) {
            System.out.println("Fehler beim Laden. Standard-Settings werden neu generiert.");
            launcher.displayMode = 0;
            launcher.language = 1;
            saveSettings(); // Repariert die Datei automatisch
        }
    }

    // --- SPIELSTAND SPEICHERN (Verschlüsselter Bytecode mit Rolling Key) ---
    public static void saveGame() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(SAVE_FILE))) {
            Random rand = new Random();
            int rollingKey = rand.nextInt(254) + 1; // Dynamischer "Autoschlüssel"-Key
            dos.writeByte(rollingKey);

            int unlocked = launcher.unlockedLevel;
            dos.writeInt(unlocked ^ rollingKey); // XOR Manipulationsschutz

            for (int i = 0; i < 3; i++) {
                long bits = Double.doubleToLongBits(launcher.bestTimes[i]);
                int part1 = (int)(bits >> 32);
                int part2 = (int)bits;
                dos.writeInt(part1 ^ rollingKey);
                dos.writeInt(part2 ^ rollingKey);
            }
            AuthService.saveProgressToBackend();
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern des Spielstands.");
        }
    }

    // --- SPIELSTAND LADEN ---
    public static void loadGame() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int rollingKey = dis.readByte() & 0xFF;

            int unlocked = dis.readInt() ^ rollingKey;
            if (unlocked < 1 || unlocked > 3) throw new Exception("Anti-Cheat!");
            launcher.unlockedLevel = unlocked;

            for (int i = 0; i < 3; i++) {
                int part1 = dis.readInt() ^ rollingKey;
                int part2 = dis.readInt() ^ rollingKey;
                long bits = ((long)part1 << 32) | (part2 & 0xFFFFFFFFL);
                launcher.bestTimes[i] = Double.longBitsToDouble(bits);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Cheat-Versuch erkannt! Setze Stand zurück.");
            launcher.unlockedLevel = 1;
            launcher.bestTimes = new double[]{-1.0, -1.0, -1.0};
            saveGame();
        }
    }

    private static String fetchJsonValue(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return "0";
        int colonIndex = json.indexOf(":", keyIndex);
        int commaIndex = json.indexOf(",", colonIndex);
        if (commaIndex == -1) commaIndex = json.indexOf("}", colonIndex);
        return json.substring(colonIndex + 1, commaIndex).trim();
    }
}
