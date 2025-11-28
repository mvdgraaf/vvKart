package me.Codex.vvKart.Listeners;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models. Checkpoint;
import me. Codex.vvKart. Models.Race;
import me.Codex.vvKart.Models.Racer;
import me.Codex.vvKart.Models.Track;
import me.Codex.vvKart.Utils.Message;
import org.bukkit. Location;
import org.bukkit. Sound;
import org.bukkit. entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CheckpointListener extends BukkitRunnable {

    private final Main plugin;

    public CheckpointListener(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Race race = plugin.getRaceManager().getRace(player);

            if (race == null) {
                continue;
            }
            if (race.getState() != Race.RaceState.RACING) continue;

            Racer racer = race.getRacer(player);
            if (racer == null || racer.isFinished()) continue;

            Location location;
            if (racer.getMinecart() != null && racer.getMinecart().isValid()) {
                location = racer.getMinecart().getLocation();
            } else {
                location = player.getLocation();
            }
            Track track = race.getTrack();

            // Check checkpoints
            checkCheckpoints(racer, location, track);

            // Check finish line
            checkFinishLine(racer, location, track);
        }
    }

    private void checkCheckpoints(Racer racer, Location playerLoc, Track track) {
        List<Checkpoint> checkpoints = track.getCheckpoints();

        for (Checkpoint checkpoint : checkpoints) {
            // Skip als al gepasseerd
            if (racer.getPassedCheckpoints().contains(checkpoint. getNumber())) {
                continue;
            }

            // Check of speler in checkpoint is
            if (checkpoint.isInside(playerLoc)) {
                racer.passCheckpoint(checkpoint. getNumber());

                Player player = racer.getPlayer();
                if (player != null) {
                    player.playSound(playerLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                    Message.sendActionBar(player, "&a✓ Checkpoint " + checkpoint.getNumber() + " passed!");
                }
            }
        }
    }

    private void checkFinishLine(Racer racer, Location playerLoc, Track track) {
        // Check if player is in finish zone
        if (! track.isInFinishZone(playerLoc)) {
            return;
        }

        int requiredCheckpoints = track.getCheckpoints().size();
        int passedCheckpoints = racer.getPassedCheckpoints().size();

        // Check if all checkpoints passed
        if (passedCheckpoints < requiredCheckpoints) {
            // Not all checkpoints passed - can't finish lap
            Player player = racer.getPlayer();
            if (player != null && racer.getCurrentCheckpoint() == 0) {
                // Only show message once
                Message.sendActionBar(player, "&cJe moet alle checkpoints passeren!  (&e" + passedCheckpoints + "/" + requiredCheckpoints + "&c)");
            }
            return;
        }

        // All checkpoints passed - complete lap
        racer.completeLap();

        Player player = racer.getPlayer();
        int currentLap = racer.getCurrentLap();
        int maxLaps = track.getLaps();

        if (currentLap >= maxLaps) {
            // Race finished!
            plugin.getRaceManager().finishRacer(racer);
        } else {
            // Lap completed
            if (player != null) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                Message.sendTitle(player, "&aRonde " + currentLap + "/" + maxLaps, "&eGa door!", 5, 30, 10);
            }
        }
    }
}