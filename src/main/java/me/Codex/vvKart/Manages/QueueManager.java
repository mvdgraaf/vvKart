package me.Codex.vvKart.Manages;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.Track;
import me.Codex.vvKart.Utils.Message;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class QueueManager {

    private final Main plugin;
    private final Map<Track, Set<UUID>> queues;
    private final Map<Track, BukkitTask> countdownTasks;
    private final Map<Track, BukkitTask> statusTasks;
    private final Map<Track, Integer> countdownSeconds;

    public QueueManager(Main plugin) {
        this.plugin = plugin;
        this.queues = new HashMap<>();
        this.countdownTasks = new HashMap<>();
        this.statusTasks = new HashMap<>();
        this.countdownSeconds = new HashMap<>();
    }

    public boolean addToQueue(Player player, Track track) {
        if(isInAnyQueue(player)){
            Message.send(player, "already-in-queue");
            return false;
        }
        if (plugin.getRaceManager().isInRace(player)) {
            Message.send(player, "already-in-race");
            return false;
        }

        if(!track.isOpen()) {
            Message.send(player, "track-closed", "track", track.getName());
            return false;
        }

        if (!plugin.getTrackManager().isTrackReady(track)) {
            Message.send(player, "track-not-ready", "track", track.getName());
            return false;
        }

        queues.computeIfAbsent(track, k -> new HashSet<>()).add(player.getUniqueId());
        Message.send(player, "joined-queue", "track", track.getName());

        if(!statusTasks.containsKey(track)) {
            startStatusUpdates(track);
        }

        int queueSize = queues.get(track).size();
        int minPlayers = track.getMinPlayers();

        for (Player p : getQueuePlayers(track)) {
            if (!p.equals(player)) {
                p.sendMessage("§e" + player.getName() + " §7is toegevoegd aan de wachtrij!  §f(" + queueSize + "/" + minPlayers + ")");
            }
        }

        checkQueueStart(track);

        return true;

    }

    public boolean removeFromQueue(Player player) {
        for (Map.Entry<Track, Set<UUID>> entry : queues.entrySet()) {
            if (entry.getValue().remove(player.getUniqueId())) {
                Track track = entry.getKey();
                Message.send(player, "left-queue", "track", track.getName());

                for (Player p : getQueuePlayers(track)) {
                    Message.send(p, "player-left-queue", "player", player.getName(), "track", track.getName());
                }

                int minPlayers = track.getMinPlayers();
                if (entry.getValue().size() < minPlayers) {
                    cancelCountdwon(track);
                }
                if (entry.getValue().isEmpty()) {
                    queues.remove(track);
                    stopStatusUpdates(track);
                }
                return true;
            }
        }
        return false;
    }

    public void checkQueueStart(Track track) {

    }

    public List<Player> getQueuePlayers(Track track) {
        List<Player> players = new ArrayList<>();
        Set<UUID> queue = queues.get(track);

        if (queue != null) {
            for (UUID id : queue) {
                Player player = plugin.getServer().getPlayer(id);
                if (player != null) players.add(player);
            }
        }
        return players;
    }

    private void startStatusUpdates(Track track) {

    }

    public void stopStatusUpdates(Track track) {
        BukkitTask task = statusTasks.remove(track);
        if (task != null) task.cancel();
    }

    public void cancelCountdwon(Track track) {

    }

    public boolean isInAnyQueue(Player player) {
        for (Set<UUID> queue : queues.values()) {
            if (queue.contains(player.getUniqueId())) return true;
        }
        return false;
    }

    private void startRace(Track track) {
        List<Player> players = getQueuePlayers(track);

        BukkitTask task = countdownTasks.remove(track);
        if(task != null ) task.cancel();

        countdownSeconds.remove(track);

        stopStatusUpdates(track);

        plugin.getRaceManager().startRace(track, players);
    }

    public void clearAll() {
        for (BukkitTask task : countdownTasks.values()) task.cancel();
        countdownTasks.clear();
        for (BukkitTask task : statusTasks.values()) task.cancel();
        statusTasks.clear();

        countdownSeconds.clear();
        queues.clear();
    }
}
