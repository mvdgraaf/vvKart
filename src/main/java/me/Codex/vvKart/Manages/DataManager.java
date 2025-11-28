package me.Codex.vvKart.Manages;

import me.Codex.vvKart.Main;
import me. Codex.vvKart.Models.Checkpoint;
import me.Codex.vvKart.Models.LeaderboardEntry;
import me.Codex.vvKart.Models.Track;
import me. Codex.vvKart. Utils.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util. Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final Main plugin;
    private final File tracksFolder;
    private final File leaderboardFile;

    public DataManager(Main plugin) {
        this.plugin = plugin;
        this.tracksFolder = new File(plugin.getDataFolder(), "tracks");
        this.leaderboardFile = new File(plugin.getDataFolder(), "leaderboard. yml");

        if (!tracksFolder.exists()) tracksFolder. mkdir();
    }

    public void loadAll(){
        loadTracks();
        loadLeaderboard();
    }

    public void saveAll(){
        saveTracks();
        saveLeaderboard();
    }

    public void loadTracks() {
        File[] files = tracksFolder.listFiles((dir, name) -> name. endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String trackName = file.getName().replace(".yml", "");

            Track track = new Track(trackName, plugin);

            track.setOpen(config.getBoolean("open", false));
            track.setLaps(config.getInt("laps", 3)); // ← Voeg laps toe

            if (config. contains("hub")) {
                Location hub = LocationUtil.deserialize(config.getString("hub"));
                track.setHub(hub);
            }

            // ========== NIEUWE FINISH ZONE LOADING ==========
            if (config.contains("finish-zone. pos1") && config.contains("finish-zone.pos2")) {
                // Nieuwe format: finish zone met 2 posities
                Location finishPos1 = LocationUtil.deserialize(config.getString("finish-zone.pos1"));
                Location finishPos2 = LocationUtil. deserialize(config.getString("finish-zone.pos2"));

                if (finishPos1 != null && finishPos2 != null) {
                    track.setFinishZone(finishPos1, finishPos2);
                }
            } else if (config.contains("finish")) {
                // Backwards compatibility: oude format (enkel punt)
                Location finish = LocationUtil.deserialize(config.getString("finish"));
                if (finish != null) {
                    track.setFinish(finish);
                }
            }
            // =================================================

            if (config.contains("start-positions")) {
                ConfigurationSection startSection = config.getConfigurationSection("start-positions");
                if (startSection != null) {
                    for (String key : startSection.getKeys(false)) {
                        try {
                            int position = Integer. parseInt(key);
                            Location loc = LocationUtil.deserialize(startSection.getString(key));
                            if (loc != null) {
                                track.setStartPosition(position, loc);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Could not load start position " + key + " for track " + trackName);
                        }
                    }
                }
            }

            if (config.contains("checkpoints")) {
                ConfigurationSection checkpointSection = config.getConfigurationSection("checkpoints");
                if (checkpointSection != null) {
                    for (String key : checkpointSection.getKeys(false)) {
                        try {
                            int number = Integer.parseInt(key);
                            ConfigurationSection cpSection = checkpointSection.getConfigurationSection(key);

                            if (cpSection != null) {
                                Location pos1 = LocationUtil. deserialize(cpSection.getString("pos1"));
                                Location pos2 = LocationUtil.deserialize(cpSection.getString("pos2"));

                                if (pos1 != null && pos2 != null) {
                                    Checkpoint checkpoint = new Checkpoint(number, pos1, pos2);
                                    track. addCheckpoint(checkpoint);
                                } else {
                                    plugin.getLogger().warning("Skipping checkpoint " + number + " for track " + trackName + " (invalid location data)");
                                }
                            }
                        } catch (Exception e) {
                            plugin.getLogger(). warning("Could not load checkpoint " + key + " for track " + trackName + ": " + e. getMessage());
                        }
                    }
                }
            }

            if (config.contains("leaderboard")) {
                Location leaderboard = LocationUtil.deserialize(config.getString("leaderboard"));
                if (leaderboard != null) {
                    track.setLeaderboard(leaderboard);
                }
            }

            plugin.getTrackManager().addTrack(track);
        }

        plugin.getLogger().info("Loaded " + (files != null ? files.length : 0) + " tracks!");
    }

    public void saveTracks() {
        for (Track track : plugin.getTrackManager().getAllTracks()) {
            saveTrack(track);
        }
    }

    public void saveTrack(Track track) {
        File file = new File(tracksFolder, track.getName() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("open", track.isOpen());
        config.set("laps", track.getLaps()); // ← Voeg laps toe

        if (track. getHub() != null) {
            config.set("hub", LocationUtil.serialize(track.getHub()));
        }

        // ========== NIEUWE FINISH ZONE SAVING ==========
        if (track.getFinishPos1() != null && track.getFinishPos2() != null) {
            // Save as finish zone
            config.set("finish-zone.pos1", LocationUtil.serialize(track.getFinishPos1()));
            config.set("finish-zone.pos2", LocationUtil.serialize(track.getFinishPos2()));
            // Remove old format if it exists
            config.set("finish", null);
        } else if (track.getFinish() != null) {
            // Backwards compatibility: save old format
            config.set("finish", LocationUtil.serialize(track. getFinish()));
            config.set("finish-zone", null);
        }
        // =================================================

        Map<Integer, Location> startPositions = track.getStartPositions();
        if (! startPositions.isEmpty()) {
            for (Map.Entry<Integer, Location> entry : startPositions.entrySet()) {
                config.set("start-positions." + entry.getKey(), LocationUtil.serialize(entry.getValue()));
            }
        }

        List<Checkpoint> checkpoints = track.getCheckpoints();
        if (!checkpoints.isEmpty()) {
            for (Checkpoint checkpoint : checkpoints) {
                String path = "checkpoints." + checkpoint.getNumber();
                config.set(path + ".pos1", LocationUtil.serialize(checkpoint.getPoint1()));
                config.set(path + ".pos2", LocationUtil. serialize(checkpoint.getPoint2()));
            }
        }

        if (track.getLeaderboard() != null) {
            config.set("leaderboard", LocationUtil.serialize(track.getLeaderboard()));
        }

        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save track: " + track.getName());
            throw new RuntimeException(e);
        }
    }

    public void deleteTrack(String trackName) {
        File file = new File(tracksFolder, trackName + ".yml");
        if (file.exists()) file.delete();
    }

    public void loadLeaderboard() {
        if (!leaderboardFile. exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(leaderboardFile);

        for (Track track : plugin. getTrackManager().getAllTracks()) {
            String trackName = track.getName();

            if (config.contains(trackName + ". fastest")) {
                ConfigurationSection fastestSection = config.getConfigurationSection(trackName + ".fastest");
                List<LeaderboardEntry> entries = new ArrayList<>();

                if (fastestSection != null) {
                    for (String key : fastestSection.getKeys(false)) {
                        ConfigurationSection entrySection = fastestSection. getConfigurationSection(key);

                        if (entrySection != null) {
                            String uuidString = entrySection.getString("uuid");
                            String playerName = entrySection.getString("player");
                            long time = entrySection.getLong("time");
                            int position = entrySection.getInt("position");
                            long date = entrySection.getLong("date", System.currentTimeMillis());

                            UUID playerUUID = uuidString != null ? UUID.fromString(uuidString) : null;

                            entries.add(new LeaderboardEntry(playerUUID, playerName, time, position, date));
                        }
                    }
                }

                entries.sort(Comparator.comparingLong(LeaderboardEntry::getTime));

                for (LeaderboardEntry entry : entries) {
                    track.addFastestTime(entry);
                }
            }
        }

        plugin.getLogger().info("Loaded leaderboards!");
    }

    public void saveLeaderboard() {
        YamlConfiguration config = new YamlConfiguration();

        for (Track track : plugin.getTrackManager().getAllTracks()) {
            String trackName = track.getName();
            List<LeaderboardEntry> fastestTimes = track.getFastestTimes();

            if (! fastestTimes.isEmpty()) {
                int index = 0;
                for (LeaderboardEntry entry : fastestTimes) {
                    String path = trackName + ".fastest." + index;

                    if (entry.getPlayerUUID() != null) {
                        config.set(path + ".uuid", entry.getPlayerUUID().toString());
                    }
                    config.set(path + ".player", entry.getPlayerName());
                    config.set(path + ".time", entry. getTime());
                    config. set(path + ".position", entry.getPosition());
                    config.set(path + ".date", entry.getDate());

                    index++;
                }
            }
        }

        try {
            config.save(leaderboardFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save leaderboards: " + e.getMessage());
            e.printStackTrace();
        }
    }
}