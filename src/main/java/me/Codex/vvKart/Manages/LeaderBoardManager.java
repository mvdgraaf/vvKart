package me.Codex.vvKart.Manages;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.LeaderboardEntry;
import me.Codex.vvKart.Models.Track;
import me.Codex.vvKart.Utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderBoardManager {

    private final Main plugin;
    private static final int MAX_ENTRIES = 10;

    private final Map<Track, TextDisplay> leaderboardDisplays;

    public LeaderBoardManager(Main plugin) {
        this.plugin = plugin;
        this.leaderboardDisplays = new HashMap<>();
    }

    public boolean createLeaderboard(Track track, Location locaction) {

        removeLeaderboard(track);

        Location displayLocation = locaction.clone().add(0, 1, 0);
        World world = locaction.getWorld();

        if (world != null) {
            TextDisplay display = world.spawn(displayLocation, TextDisplay.class, entity -> {
                entity.setSeeThrough(true);
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setViewRange(50.0f);

                Component text = buildLeaderboardText(track);
                entity.text(text);
            });
            leaderboardDisplays.put(track, display);
            track.setLeaderboard(displayLocation);
            plugin.getDataManager().saveTrack(track);
        } else {
            return false;
        }
        return true;
    }

    private Component buildLeaderboardText(Track track) {
        String title = "⚡ Leaderboard " + track.getName() + " ⚡";

        // Create dynamic line
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < title.length(); i++) {
            line.append("▬");
        }

        // Get top entries
        List<LeaderboardEntry> entries = track.getFastestTimes(MAX_ENTRIES);

        // Build component using text() method
        Component text = Component.text(title + "\n", TextColor.fromHexString("#FFD700"))  // Gold
                .append(Component.text(line.toString() + "\n\n", NamedTextColor.WHITE));

        // Add entries
        for (int i = 0; i < MAX_ENTRIES; i++) {
            if (i < entries.size()) {
                LeaderboardEntry entry = entries.get(i);
                String position = (i + 1) + ". ";
                String playerName = entry.getPlayerName();
                String time = Time.formatTime(entry.getFinishTime());

                // Color based on position
                TextColor color = getPositionColor(i + 1);

                text = text.append(Component.text(position, color))
                        .append(Component.text(playerName + " ", NamedTextColor.WHITE))
                        . append(Component.text("- ", NamedTextColor.DARK_GRAY))
                        . append(Component.text(time, NamedTextColor.YELLOW));

                // Add newline except for last entry
                if (i < MAX_ENTRIES - 1) {
                    text = text.append(Component.text("\n"));
                }
            } else {
                // Empty slot
                String position = (i + 1) + ". ";

                text = text.append(Component.text(position, NamedTextColor.DARK_GRAY))
                        .append(Component.text("---", NamedTextColor.DARK_GRAY));

                if (i < MAX_ENTRIES - 1) {
                    text = text.append(Component.text("\n"));
                }
            }
        }

        return text;
    }

    public void updateLeaderboard(Track track) {

        TextDisplay display = leaderboardDisplays.get(track);

        if (display != null && track.getLeaderboard() != null) {
            createLeaderboard(track, track.getLeaderboard());
            display = leaderboardDisplays.get(track);
        }

        if (display == null || !display.isValid()) {
            return;
        }

        Component text = buildLeaderboardText(track);
        display.text(text);
    }

    public boolean removeLeaderboard(Track track) {
        TextDisplay display = leaderboardDisplays.remove(track);

        if (display != null && display.isValid()) {
            display.remove();
            track.setLeaderboard(null);
            plugin.getDataManager().saveTrack(track);
            return true;
        }
        return false;
    }

    public void loadAllLeaderboards() {
        for (Track track : plugin.getTrackManager().getAllTracks()) {
            if (track.getLeaderboard() != null) {
                createLeaderboard(track, track.getLeaderboard());
            }
        }
    }

    public void removeAllLeaderboards() {
        for (TextDisplay display : leaderboardDisplays.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        leaderboardDisplays.clear();
    }

    public void updateAllLeaderboards() {
        for (Track track : plugin.getTrackManager(). getAllTracks()) {
            updateLeaderboard(track);
        }
    }

    private TextColor getPositionColor(int position) {
        return switch (position) {
            case 1 -> TextColor.fromHexString("#FFD700");  // Gold
            case 2 -> TextColor.fromHexString("#C0C0C0");  // Silver
            case 3 -> TextColor.fromHexString("#CD7F32");  // Bronze
            default -> NamedTextColor.WHITE;
        };
    }
}
