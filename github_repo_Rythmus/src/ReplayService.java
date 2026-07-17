import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReplayService {
    private static final String REPLAY_DIR = "replays";

    public static void saveReplay(int level, double finalTime, String framesJson) {
        String replayId = "replay_" + System.currentTimeMillis();
        String replayJson = "{"
                + "\"id\":\"" + replayId + "\","
                + "\"ownerId\":\"" + escapeJson(AuthService.getCurrentUserId()) + "\","
                + "\"player\":\"" + escapeJson(AuthService.getCurrentDisplayName()) + "\","
                + "\"level\":" + level + ","
                + "\"time\":" + finalTime + ","
                + "\"createdAt\":" + System.currentTimeMillis() + ","
                + "\"frames\":[" + framesJson + "]"
                + "}";

        saveReplayFile(replayId, replayJson);
        AuthService.saveReplay(replayId, replayJson);
    }

    private static void saveReplayFile(String replayId, String replayJson) {
        File dir = new File(REPLAY_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            System.out.println("Replay-Ordner konnte nicht erstellt werden.");
            return;
        }

        File file = new File(dir, replayId + ".json");
        try (java.io.Writer writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(replayJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
