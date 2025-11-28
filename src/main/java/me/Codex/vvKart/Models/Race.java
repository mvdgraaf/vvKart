package me.Codex.vvKart.Models;

import me.Codex.vvKart.Main;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Race {

    private final Main plugin;

    private final Track track;
    private final Map<UUID, Racer> racers;
    private RaceState state;

    private BukkitTask updateTask;

    private long startTime;
    private long endTime;
    private int countdownSeconds;

    private UUID firstFinisher;
    private long finishCountdownStart;
    private int timeoutSeconds = 30;

    public Race(Track track, Main plugin) {
        this.plugin = plugin;
        this.track = track;
        this.racers = new HashMap<>();
        this.state = RaceState.WAITING;
        this.countdownSeconds = plugin.getConfig().getInt("race.queue-wait-time");
    }

    public boolean addRacer(Player player) {
        if (racers.size() >= plugin.getConfig().getInt("race.max-players", 10) ) {
            return false;
        }

        if (state != RaceState.WAITING && state != RaceState.COUNTDOWN) {
            return false;
        }

        Racer racer = new Racer(player, this);
        racers.put(player.getUniqueId(), racer);
        return true;
    }

    public Racer removeRacer(UUID player) {
        if (racers.containsKey(player)) {
            return racers.remove(player);
        }
        return null;
    }

    public Racer getRacer(UUID player) {
        return racers.get(player);
    }

    public Racer getRacer(Player player) {
        return racers.get(player.getUniqueId());
    }

    public boolean hasRacer(UUID player) {
        return racers.containsKey(player);
    }

    public Collection<Racer> getRacers() {
        return racers.values();
    }

    public List<Racer> getRacersByPosition() {
        List<Racer> sortedRacers = new ArrayList<>(racers.values());
        sortedRacers.sort(Comparator.comparingInt(Racer::getPosition));
        return sortedRacers;
    }

    public List<Racer> getRacersByFinishTime() {
        List<Racer> finished = new ArrayList<>();
        for(Racer racer : racers.values()) {
            if (racer.isFinished() && !racer.isDisqualified()) {
                finished.add(racer);
            }
        }
        finished.sort(Comparator.comparingLong(Racer::getFinishTime));
        return finished;
    }

    public int getRacerCount() {
        return racers.size();
    }

    public int getFinishedCount() {
        return (int) racers.values().stream().filter(Racer::isFinished).count();
    }

    public void updatePositions() {
        List<Racer> sorted = new ArrayList<>(racers.values());

        // Sort by: finished first, then by lap, then by checkpoint
        sorted.sort((r1, r2) -> {
            // Finished racers come first
            if (r1.isFinished() && !r2.isFinished()) return -1;
            if (!r1.isFinished() && r2.isFinished()) return 1;

            // If both finished, sort by finish time
            if (r1.isFinished() && r2.isFinished()) {
                return Long.compare(r1.getFinishTime(), r2.getFinishTime());
            }

            // Compare by lap
            int lapCompare = Integer.compare(r2.getCurrentLap(), r1.getCurrentLap());
            if (lapCompare != 0) return lapCompare;

            // Compare by checkpoint
            return Integer.compare(r2.getCurrentCheckpoint(), r1.getCurrentCheckpoint());
        });

        // Assign positions
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setPosition(i + 1);
        }
    }

    public void setUpdateTask(BukkitTask task) {
        this.updateTask = task;
    }

    public BukkitTask getUpdateTask() {
        return this.updateTask;
    }

    public void cancelUpdateTask() {
        if (this.updateTask != null && !this.updateTask.isCancelled()) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
    }

    public void start() {
        this.state = RaceState.RACING;
        this.startTime = System.currentTimeMillis();

        for(Racer racer : racers.values()) {
            racer.setStartTime(startTime);
        }
    }

    public void end() {
        this.state = RaceState.FINISHED;
        this.endTime = System.currentTimeMillis();
    }

    public void markFirstFinisher(UUID player) {
        if (firstFinisher == null) {
            firstFinisher = player;
            finishCountdownStart = System.currentTimeMillis();
        }
    }

    public int getFinishCountdownSeconds() {
        return timeoutSeconds;
    }

    public boolean isFinishCountdownExpired() {
        if (firstFinisher == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - finishCountdownStart;
        return elapsed >= (timeoutSeconds * 1000L);
    }

    public Track getTrack() {
        return track;
    }

    public RaceState getState() {
        return state;
    }

    public void setState(RaceState state) {
        this.state = state;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public void setCountdownSeconds(int countdownSeconds) {
        this.countdownSeconds = countdownSeconds;
    }

    public UUID getFirstFinisher() {
        return firstFinisher;
    }

    public enum RaceState {
        WAITING,      // Waiting for players to join
        COUNTDOWN,    // Countdown before race starts
        RACING,       // Race in progress
        FINISHING,    // First player finished, countdown running
        FINISHED      // Race ended
    }

}
