package me.Codex.vvKart.Models;

import me.Codex.vvKart.Main;
import org.bukkit.Location;

import java.util.*;

import static me.Codex.vvKart.Main.getInstance;

public class Track {

    private final Main plugin;
    private final String name;
    private Location hub;
    private Location finish;
    private Location finishPos1;
    private Location finishPos2;
    private Map<Integer, Location> startPositions;
    private List<Checkpoint> checkpoints;
    private boolean isOpen;
    private Location Leaderboard;
    private int laps;
    private List<LeaderboardEntry> fastestTimes = new ArrayList<>();
    private final int maxLeaderboardEntries = 10;
    private int min_player;
    private int countdownSeconds;

    public Track(String name, Main plugin) {
        this.plugin = plugin;
        this.name = name;
        this.startPositions = new HashMap<>();
        this.checkpoints = new ArrayList<>();
        this.isOpen = false;
        this.laps = getInstance().getConfig().getInt("race.laps");
        this.min_player = plugin.getConfig().getInt("race.min-players", 2);
        this.countdownSeconds = getInstance().getConfig().getInt("race.queue-wait-time");
    }

    public String getName() {
        return name;
    }

    public Location getHub() {
        return hub;
    }

    public void setHub(Location hub) {
        this.hub = hub;
    }

    public boolean isInFinishZone(Location location) {
        if (finishPos1 == null || finishPos2 == null) {
            // Backwards compatibility: gebruik oude finish als punt
            if (finish != null) {
                return location.distance(finish) < 3.0;
            }
            return false;
        }

        if (! location.getWorld().equals(finishPos1.getWorld())) {
            return false;
        }

        double minX = Math.min(finishPos1.getX(), finishPos2.getX());
        double minY = Math.min(finishPos1.getY(), finishPos2.getY());
        double minZ = Math.min(finishPos1.getZ(), finishPos2.getZ());

        double maxX = Math.max(finishPos1.getX(), finishPos2.getX());
        double maxY = Math.max(finishPos1. getY(), finishPos2. getY());
        double maxZ = Math.max(finishPos1.getZ(), finishPos2.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public void setFinishZone(Location pos1, Location pos2) {
        this.finishPos1 = pos1;
        this.finishPos2 = pos2;
    }

    public Location getFinishPos1() { return finishPos1; }
    public Location getFinishPos2() { return finishPos2; }
    public Location getFinish() { return finish; }
    public void setFinish(Location finish) { this.finish = finish; }

    public int getLaps() {
        return laps;
    }

    public void setLaps(int laps) {
        this.laps = laps;
    }

    public void addLap() {
        this.laps++;
    }

    public int getMinPlayers() {
        return min_player;
    }

    public void setMinPlayers(int min_player) {
        this.min_player = min_player;
    }

    public Map<Integer, Location> getStartPositions() {
        return startPositions;
    }

    public Location getStartPosition(int position) {
        return startPositions.get(position);
    }

    public void setStartPosition(int position, Location startPositions) {
        this.startPositions.put(position, startPositions);
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public void addCheckpoint(Checkpoint checkpoint) {
        this.checkpoints.add(checkpoint);
        // Sort by checkpoint number
        this.checkpoints. sort(Comparator.comparingInt(Checkpoint::getNumber));
    }

    public boolean removeCheckpoint(int number) {
        return this.checkpoints.removeIf(cp -> cp.getNumber() == number);
    }

    public Checkpoint getCheckpoint(int number) {
        return checkpoints.stream()
                .filter(cp -> cp.getNumber() == number)
                .findFirst()
                .orElse(null);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public void setCountdownSeconds(int countdownSeconds) {
        this.countdownSeconds = countdownSeconds;
    }

    public void setLeaderboard(Location leaderboardLocation) {
        this.Leaderboard = leaderboardLocation;
    }

    public Location getLeaderboard() {
        return Leaderboard;
    }

    public void addFastestTime(LeaderboardEntry entry) {
        fastestTimes.add(entry);

        // Sort by time (fastest first)
        fastestTimes.sort(Comparator.comparingLong(LeaderboardEntry::getTime));

        // Keep only top entries
        if (fastestTimes.size() > maxLeaderboardEntries) {
            fastestTimes = fastestTimes.subList(0, maxLeaderboardEntries);
        }

        // Update positions
        for (int i = 0; i < fastestTimes.size(); i++) {
            fastestTimes.get(i). setPosition(i + 1);
        }
    }

    public List<LeaderboardEntry> getFastestTimes() {
        return new ArrayList<>(fastestTimes);
    }

    public List<LeaderboardEntry> getFastestTimes(int limit) {
        return fastestTimes.stream().limit(limit).toList();
    }

    public boolean isLeaderboardTime(long time) {
        if (fastestTimes.size() < maxLeaderboardEntries) return true;

        LeaderboardEntry lastEntry = fastestTimes.get(fastestTimes.size() - 1);
        return time < lastEntry.getTime();
    }

    public LeaderboardEntry getPlayerBestTime(UUID playerUUID) {
        return fastestTimes.stream()
                .filter(entry -> entry. getPlayerUUID() != null && entry.getPlayerUUID().equals(playerUUID))
                .min(Comparator.comparingLong(LeaderboardEntry::getTime))
                .orElse(null);
    }

    public int getPlayerPosition(UUID playerUUID) {
        LeaderboardEntry entry = getPlayerBestTime(playerUUID);
        return entry != null ? entry. getPosition() : 0;
    }

    public boolean isSetupComplete() {
        return hub != null &&
                finish != null &&
                ! startPositions.isEmpty() &&
                !checkpoints.isEmpty();
    }
}
