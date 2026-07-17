import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthService {
    private static final String SESSION_FILE = "session.token";
    private static final int OBF_KEY = 73;
    private static final String DATABASE_URL = hidden(new int[]{33,61,61,57,58,115,102,102,59,48,61,33,36,100,46,40,36,44,100,112,44,122,124,121,100,45,44,47,40,60,37,61,100,59,61,45,43,103,44,60,59,38,57,44,100,62,44,58,61,120,103,47,32,59,44,43,40,58,44,45,40,61,40,43,40,58,44,103,40,57,57});
    private static final String API_KEY = hidden(new int[]{8,0,51,40,26,48,11,0,4,122,62,112,11,122,1,63,124,48,16,15,8,3,33,63,61,123,27,0,120,19,113,16,5,32,120,35,12,61,38});

    private static String idToken = "";
    private static String displayName = "Gast";
    private static String userId = "";
    private static String email = "";

    public static boolean checkAutomaticLogin() {
        File file = new File(SESSION_FILE);
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8))) {
            idToken = reader.readLine();
            if (isBlank(idToken)) {
                return false;
            }
            if (idToken.startsWith("offline:")) {
                displayName = idToken.substring("offline:".length());
                idToken = "";
                userId = "offline_" + encodeUrl(displayName);
                return true;
            }
            displayName = readClaimFromJwt(idToken, "name", readClaimFromJwt(idToken, "email", "Spieler"));
            userId = readClaimFromJwt(idToken, "user_id", readClaimFromJwt(idToken, "sub", ""));
            email = readClaimFromJwt(idToken, "email", "");
            if (isCurrentUserBanned()) {
                logout();
                return false;
            }
            saveAccountInfo();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loginUser(String email, String password) {
        if (isBlank(API_KEY)) {
            loginOffline(email);
            return true;
        }

        String endpoint = hidden(new int[]{33,61,61,57,58,115,102,102,32,45,44,39,61,32,61,48,61,38,38,37,34,32,61,103,46,38,38,46,37,44,40,57,32,58,103,42,38,36,102,63,120,102,40,42,42,38,60,39,61,58,115,58,32,46,39,0,39,30,32,61,33,25,40,58,58,62,38,59,45,118,34,44,48,116}) + API_KEY;
        String payload = "{\"email\":\"" + escapeJson(email) + "\",\"password\":\"" + escapeJson(password) + "\",\"returnSecureToken\":true}";
        String response = sendJsonRequest(endpoint, payload);
        if (response == null || response.contains("\"error\"")) {
            return false;
        }

        idToken = extractJsonValue(response, "idToken");
        userId = extractJsonValue(response, "localId");
        email = extractJsonValue(response, "email");
        displayName = extractJsonValue(response, "displayName");
        if (isBlank(displayName)) {
            displayName = email;
        }
        if (isCurrentUserBanned()) {
            logout();
            return false;
        }
        saveSessionToken();
        saveAccountInfo();
        return !isBlank(idToken);
    }

    public static boolean registerUser(String email, String password, String name) {
        if (isBlank(API_KEY)) {
            loginOffline(isBlank(name) ? email : name);
            return true;
        }

        String endpoint = hidden(new int[]{33,61,61,57,58,115,102,102,32,45,44,39,61,32,61,48,61,38,38,37,34,32,61,103,46,38,38,46,37,44,40,57,32,58,103,42,38,36,102,63,120,102,40,42,42,38,60,39,61,58,115,58,32,46,39,28,57,118,34,44,48,116}) + API_KEY;
        String payload = "{\"email\":\"" + escapeJson(email) + "\",\"password\":\"" + escapeJson(password) + "\",\"returnSecureToken\":true}";
        String response = sendJsonRequest(endpoint, payload);
        if (response == null || response.contains("\"error\"")) {
            return false;
        }

        idToken = extractJsonValue(response, "idToken");
        userId = extractJsonValue(response, "localId");
        email = extractJsonValue(response, "email");
        displayName = name;
        updateProfile(name);
        sendEmailVerification();
        saveAccountInfo();
        saveSessionToken();
        return true;
    }

    public static boolean sendPasswordReset(String email) {
        if (isBlank(API_KEY)) {
            System.out.println("Firebase API_KEY fehlt in AuthService.java.");
            return false;
        }

        String endpoint = hidden(new int[]{33,61,61,57,58,115,102,102,32,45,44,39,61,32,61,48,61,38,38,37,34,32,61,103,46,38,38,46,37,44,40,57,32,58,103,42,38,36,102,63,120,102,40,42,42,38,60,39,61,58,115,58,44,39,45,6,38,43,10,38,45,44,118,34,44,48,116}) + API_KEY;
        String payload = "{\"requestType\":\"PASSWORD_RESET\",\"email\":\"" + escapeJson(email) + "\"}";
        String response = sendJsonRequest(endpoint, payload);
        return response != null && !response.contains("\"error\"");
    }

    public static void logout() {
        idToken = "";
        displayName = "Gast";
        userId = "";
        email = "";
        File file = new File(SESSION_FILE);
        if (file.exists() && !file.delete()) {
            System.out.println("Session-Datei konnte nicht geloescht werden.");
        }
    }

    public static String getCurrentDisplayName() {
        if (!isBlank(displayName) && !"Gast".equals(displayName) && !"Spieler".equals(displayName)) {
            return displayName;
        }
        if (!isBlank(idToken)) {
            displayName = readClaimFromJwt(idToken, "name", readClaimFromJwt(idToken, "email", "Spieler"));
        }
        return isBlank(displayName) ? "Spieler" : displayName;
    }

    public static boolean isAdmin() {
        return false;
    }

    public static String getCurrentUserId() {
        if (isBlank(userId)) {
            checkAutomaticLogin();
        }
        return isBlank(userId) ? "local" : userId;
    }

    public static void submitScore(int score) {
        submitScore(1, score);
    }

    public static void submitScore(int level, int score) {
        try {
            String rawUsername = getCurrentDisplayName();
            String encodedUsername = encodeUrl(rawUsername);
            String urlString = withAuth(DATABASE_URL + "/scores/level" + level + "/" + encodedUsername + ".json");

            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonPayload = "{\"score\":" + score + "}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean loadProgressFromBackend() {
        if (!isCloudAccountReady()) {
            return false;
        }

        String json = getJson(withAuth(DATABASE_URL + "/users/" + userId + "/progress.json"));
        if (json == null) {
            return false;
        }

        try {
            int cloudUnlockedLevel = parseIntValue(json, "unlockedLevel", launcher.unlockedLevel);
            if (cloudUnlockedLevel >= 1 && cloudUnlockedLevel <= 3) {
                launcher.unlockedLevel = cloudUnlockedLevel;
            }

            double[] cloudBestTimes = parseBestTimes(json);
            for (int i = 0; i < launcher.bestTimes.length && i < cloudBestTimes.length; i++) {
                if (cloudBestTimes[i] >= 0) {
                    launcher.bestTimes[i] = cloudBestTimes[i];
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void saveProgressToBackend() {
        if (!isCloudAccountReady()) {
            return;
        }

        String payload = "{\"unlockedLevel\":" + launcher.unlockedLevel
                + ",\"bestTimes\":["
                + launcher.bestTimes[0] + ","
                + launcher.bestTimes[1] + ","
                + launcher.bestTimes[2] + "]}";
        putJson(withAuth(DATABASE_URL + "/users/" + userId + "/progress.json"), payload);
    }

    public static boolean banAccount(String target, String reason) {
        if (!isAdmin() || isBlank(target)) {
            return false;
        }

        String payload = "{\"target\":\"" + escapeJson(target.trim()) + "\",\"reason\":\"" + escapeJson(reason) + "\",\"bannedBy\":\""
                + escapeJson(getCurrentDisplayName()) + "\",\"createdAt\":" + System.currentTimeMillis() + "}";
        putJson(withAuth(DATABASE_URL + "/bans/" + encodeKey(target.trim()) + ".json"), payload);
        return true;
    }

    public static boolean unbanAccount(String target) {
        if (!isAdmin() || isBlank(target)) {
            return false;
        }
        deleteJson(withAuth(DATABASE_URL + "/bans/" + encodeKey(target.trim()) + ".json"));
        return true;
    }

    public static String getAccountsJsonForAdmin() {
        if (!isAdmin()) {
            return "";
        }
        String json = getJson(withAuth(DATABASE_URL + "/accounts.json"));
        return json == null ? "" : json;
    }

    public static String getAllReplaysJsonForAdmin() {
        if (!isAdmin()) {
            return "";
        }
        String json = getJson(withAuth(DATABASE_URL + "/replays.json"));
        return json == null ? "" : json;
    }

    public static void saveReplay(String replayId, String replayJson) {
        if (!isCloudAccountReady()) {
            return;
        }
        putJson(withAuth(DATABASE_URL + "/replays/" + userId + "/" + replayId + ".json"), replayJson);
    }

    public static String getReplayJsonForAdmin(String ownerId, String replayId) {
        if (!isAdmin() || isBlank(ownerId) || isBlank(replayId)) {
            return null;
        }
        return getJson(withAuth(DATABASE_URL + "/replays/" + ownerId + "/" + replayId + ".json"));
    }

    public static List<String[]> getLeaderboardData(int level) {
        List<String[]> leaderboardList = new ArrayList<>();
        try {
            String json = getJson(withAuth(DATABASE_URL + "/scores/level" + level + ".json"));
            if (json != null) {
                addLeaderboardEntries(leaderboardList, json);
            }

            if (leaderboardList.isEmpty() && level == 1) {
                String legacyJson = getJson(withAuth(DATABASE_URL + "/scores.json"));
                if (legacyJson != null) {
                    addLeaderboardEntries(leaderboardList, legacyJson);
                }
            }

            leaderboardList.sort((a, b) -> Integer.compare(Integer.parseInt(a[1]), Integer.parseInt(b[1])));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return leaderboardList;
    }

    private static String getJson(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                System.out.println("Leaderboard konnte nicht geladen werden. HTTP " + conn.getResponseCode());
                return null;
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                String json = response.toString().trim();
                return json.equals("null") || json.isEmpty() ? null : json;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void putJson(String urlString, String payload) {
        try {
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                System.out.println("Fortschritt konnte nicht gespeichert werden. HTTP " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteJson(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            int responseCode = conn.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                System.out.println("Eintrag konnte nicht geloescht werden. HTTP " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addLeaderboardEntries(List<String[]> leaderboardList, String json) {
        Pattern pattern = Pattern.compile("\"([^\"]+)\":\\s*\\{\\s*\"score\"\\s*:\\s*([0-9]+)\\s*\\}");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String name = decodeUrl(matcher.group(1));
            if (!name.startsWith("level")) {
                leaderboardList.add(new String[]{name, matcher.group(2)});
            }
        }
    }

    private static String withAuth(String url) {
        if (isBlank(idToken)) {
            checkAutomaticLogin();
        }
        if (isBlank(idToken)) {
            return url;
        }
        return url + "?auth=" + encodeUrl(idToken);
    }

    private static boolean isCloudAccountReady() {
        if (isBlank(idToken)) {
            checkAutomaticLogin();
        }
        return !isBlank(idToken) && !isBlank(userId);
    }

    private static boolean isCurrentUserBanned() {
        if (isAdmin()) {
            return false;
        }
        return isBannedKey(userId) || isBannedKey(displayName) || isBannedKey(email);
    }

    private static boolean isBannedKey(String value) {
        if (isBlank(value)) {
            return false;
        }
        String json = getJson(withAuth(DATABASE_URL + "/bans/" + encodeKey(value) + ".json"));
        return json != null;
    }

    private static void saveAccountInfo() {
        if (!isCloudAccountReady()) {
            return;
        }
        String payload = "{\"userId\":\"" + escapeJson(userId) + "\",\"displayName\":\"" + escapeJson(getCurrentDisplayName())
                + "\",\"email\":\"" + escapeJson(email) + "\",\"admin\":" + isAdmin() + ",\"lastSeen\":" + System.currentTimeMillis() + "}";
        putJson(withAuth(DATABASE_URL + "/accounts/" + userId + ".json"), payload);
    }

    private static void updateProfile(String name) {
        String endpoint = hidden(new int[]{33,61,61,57,58,115,102,102,32,45,44,39,61,32,61,48,61,38,38,37,34,32,61,103,46,38,38,46,37,44,40,57,32,58,103,42,38,36,102,63,120,102,40,42,42,38,60,39,61,58,115,60,57,45,40,61,44,118,34,44,48,116}) + API_KEY;
        String payload = "{\"idToken\":\"" + escapeJson(idToken) + "\",\"displayName\":\"" + escapeJson(name) + "\",\"returnSecureToken\":true}";
        String response = sendJsonRequest(endpoint, payload);
        if (response != null && !response.contains("\"error\"")) {
            String newToken = extractJsonValue(response, "idToken");
            if (!isBlank(newToken)) {
                idToken = newToken;
            }
        }
    }

    private static void sendEmailVerification() {
        String endpoint = hidden(new int[]{33,61,61,57,58,115,102,102,32,45,44,39,61,32,61,48,61,38,38,37,34,32,61,103,46,38,38,46,37,44,40,57,32,58,103,42,38,36,102,63,120,102,40,42,42,38,60,39,61,58,115,58,44,39,45,6,38,43,10,38,45,44,118,34,44,48,116}) + API_KEY;
        String payload = "{\"requestType\":\"VERIFY_EMAIL\",\"idToken\":\"" + escapeJson(idToken) + "\"}";
        sendJsonRequest(endpoint, payload);
    }

    private static String sendJsonRequest(String endpoint, String payload) {
        try {
            URL url = new URI(endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            InputStream stream = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void saveSessionToken() {
        if (isBlank(idToken)) {
            return;
        }

        try (java.io.Writer writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(SESSION_FILE), StandardCharsets.UTF_8)) {
            writer.write(idToken);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loginOffline(String name) {
        displayName = isBlank(name) ? "Spieler" : name;
        idToken = "";
        userId = "offline_" + encodeUrl(displayName);
        try (java.io.Writer writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(SESSION_FILE), StandardCharsets.UTF_8)) {
            writer.write("offline:" + displayName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static int parseIntValue(String json, String key, int fallback) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private static double[] parseBestTimes(String json) {
        double[] values = {-1.0, -1.0, -1.0};
        Pattern pattern = Pattern.compile("\"bestTimes\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return values;
        }

        String[] parts = matcher.group(1).split(",");
        for (int i = 0; i < values.length && i < parts.length; i++) {
            values[i] = Double.parseDouble(parts[i].trim());
        }
        return values;
    }

    private static String readClaimFromJwt(String jwt, String key, String fallback) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) {
                return fallback;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String value = extractJsonValue(payload, key);
            return isBlank(value) ? fallback : value;
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String encodeKey(String value) {
        return encodeUrl(value).replace(".", "%2E");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String encodeUrl(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return "";
        }
    }

    private static String decodeUrl(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    private static String hidden(int[] data) {
        char[] chars = new char[data.length];
        for (int i = 0; i < data.length; i++) {
            chars[i] = (char)(data[i] ^ OBF_KEY);
        }
        return new String(chars);
    }
}
