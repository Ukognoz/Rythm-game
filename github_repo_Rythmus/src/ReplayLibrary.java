import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;

public class ReplayLibrary extends JPanel {
    private final JList<File> replayList = new JList<>();
    private final Color bgColor = new Color(24, 24, 32);

    public ReplayLibrary(int width, int height) {
        setLayout(new BorderLayout(12, 12));
        setBackground(bgColor);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("REPLAYS");
        title.setForeground(new Color(255, 85, 85));
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        add(title, BorderLayout.NORTH);

        replayList.setBackground(new Color(32, 32, 42));
        replayList.setForeground(Color.WHITE);
        replayList.setFont(new Font("Consolas", Font.PLAIN, 13));
        replayList.setListData(loadReplayFiles());
        add(new JScrollPane(replayList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 8));
        buttons.setBackground(bgColor);
        JButton playBtn = button("Abspielen");
        JButton backBtn = button("Zurueck");
        buttons.add(playBtn);
        buttons.add(backBtn);
        add(buttons, BorderLayout.SOUTH);

        playBtn.addActionListener(event -> playSelectedReplay());
        backBtn.addActionListener(event -> launcher.showLobby());
    }

    private File[] loadReplayFiles() {
        File dir = new File("replays");
        File[] files = dir.listFiles((folder, name) -> name.endsWith(".json"));
        return files == null ? new File[0] : files;
    }

    private void playSelectedReplay() {
        File file = replayList.getSelectedValue();
        if (file == null) {
            return;
        }

        try {
            String replayJson = new String(Files.readAllBytes(file.toPath()), "UTF-8");
            launcher.window.getContentPane().removeAll();
            ReplayViewer viewer = new ReplayViewer(launcher.currentWidth, launcher.currentHeight, replayJson);
            launcher.window.add(viewer);
            launcher.window.revalidate();
            launcher.window.repaint();
            viewer.requestFocusInWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JButton button(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }
}
