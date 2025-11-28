package me.Codex.vvKart.Manages;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.LeaderboardEntry;
import me.Codex.vvKart.Models.Race;
import me.Codex.vvKart.Models.Racer;
import me.Codex.vvKart.Models.Track;
import me.Codex.vvKart.Utils.Message;
import me.Codex.vvKart.Utils.Time;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RaceManager {

    private final Main plugin;
    private final Map<Track, Race> activeRaces;
    private final Map<UUID, Race> playerRaces;
    private final Map<UUID, PlayerData> savedPlayerData;

    public RaceManager(Main plugin) {
        this.plugin = plugin;
        this.activeRaces = new HashMap<>();
        this.playerRaces = new HashMap<>();
        this.savedPlayerData = new HashMap<>();
    }

    public void startRace(Track track, List<Player> players){
        if (activeRaces.containsKey(track)) {
            plugin.getLogger().warning("Race already active on track: " + track.getName());
            return;
        }

        Race race = new Race(track);
        activeRaces.put(track, race);

        int position = 1;
        for (Player player : players) {
            race.addRacer(player);
            playerRaces.put(player.getUniqueId(), race);

            Location startPos = track.getStartPosition(position);
            if (startPos != null) {
                player.teleport(startPos);

                Minecart minecart = startPos.getWorld().spawn(startPos, Minecart.class);
                minecart.addPassenger(player);
                race.getRacer(player.getUniqueId()).setMinecart(minecart);
            }
            race.getRacer(player).saveInventory();
            race.getRacer(player).clearInventory();
            race.getRacer(player).giveLeaveItem();
            player.setGameMode(GameMode.ADVENTURE);

            position++;
        }
        startCountdown(race);
    }

    private void startCountdown(Race race) {
        race.setState(Race.RaceState.COUNTDOWN);

        final int[] countdown = {3};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (countdown[0] > 0) {
                for (Racer racer : race.getRacers()) {
                    Player player = racer.getPlayer();
                    if (player != null) {
                        Message.sendTitle(player, "&e" + countdown[0], "", 0, 20, 10);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                    }
                }
                countdown[0]--;
            } else {
                startRacing(race);
            }
        }, 0L, 20L);

        Bukkit.getScheduler().runTaskLater(plugin, task::cancel, 80L);
    }

    private void startRacing(Race race) {
        race.setState(Race.RaceState.RACING);
        for (Racer racer : race.getRacers()) {
            Player player = racer.getPlayer();
            if (player != null) {
                Message.sendTitle(player, "&a&lGO!", "", 0, 20, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
            }
        }
        race.start();
        startPositionUpdateTask(race);
    }

    private void startPositionUpdateTask(Race race) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (race.getState() != Race.RaceState.RACING) return;

            race.updatePositions();

            for (Racer racer : race.getRacers()) {
                if (racer.isFinished()) continue;

                Player player = racer.getPlayer();
                if (player != null) {
                    long elapsedTime = racer.getElapsedTime();
                    int currentLap = racer.getCurrentLap();
                    int maxLaps = race.getTrack().getLaps();
                    int position = racer.getPosition();

                    Message.sendActionBar(player, String.format("&e⏱ %s &7| &bRonde: &f%d/%d &7| &6Positie: &f%d",
                            Time.formatTime(elapsedTime),
                            currentLap,
                            maxLaps,
                            position));
                }
            }
        }, 0L, 20L);

        race.setUpdateTask(task);
    }

    public void finishRacer(Racer racer) {
        if (racer.isFinished()) return;

        racer.setFinishTime(System.currentTimeMillis());
        racer.setFinished(true);

        Player player = racer.getPlayer();
        if (player != null) {
            Message.sendTitle(player, "&a&lFINISHED!", Color.BLUE + Time.formatTime(racer.getElapsedTime()), 0, 20, 10);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
            Message.send(player,"race-finished",
                    "position", String.valueOf(racer.getPosition()),
                    "time", Time.formatTime(racer.getElapsedTime()));
            racer.restoreInventory();
            player.teleport(racer.getRace().getTrack().getHub());
            racer.getMinecart().remove();
        }

        Race race = racer.getRace();

        long finishedCount = race.getRacers().stream(). filter(Racer::isFinished).count();
        if (finishedCount == 1) {
            startFinishTimeout(race);
            race.markFirstFinisher(player.getUniqueId());
        }

        if (finishedCount == race.getRacers().size()) {
            endRace(race);
            showResults(race);
        }
    }

    private void startFinishTimeout(Race race) {
        int timeout = race.getFinishCountdownSeconds();
        for (Racer racer : race.getRacers()) {
            Player player = racer.getPlayer();
            if (player != null) {
                Message.send(player, "&c&lFirst player finished!\n&eYou will be teleported back in " + timeout + " seconds.");
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (race.getState() == Race.RaceState.RACING) {
                endRace(race);
            }
        }, timeout * 20L);
    }

    private void endRace(Race race) {
        race.end();
        race.setState(Race.RaceState.FINISHED);
        if (race.getUpdateTask() != null) race.cancelUpdateTask();
        for (Racer racer : race.getRacers()) {
            Player player = racer.getPlayer();
            if (racer.isFinished()) {
                giveRewards(player, racer);
            }
            playerRaces.remove(racer.getPlayerUUID());
        }
        updateLeaderboard(race);
        activeRaces.remove(race.getTrack());
    }

    public boolean isInRace(Player player) {
        return playerRaces.containsKey(player.getUniqueId());
    }

    public Racer getRacer(Player player) {
        Race race = playerRaces.get(player.getUniqueId());
        return race != null ? race.getRacer(player) : null;
    }

    public void cleanup() {
        for (Track track : new HashSet<>(activeRaces.keySet())) {
            cancelRace(track);
        }
    }

    public void removeFromRace(Player player) {
        Race race = playerRaces.remove(player.getUniqueId());
        if (race != null) {
            Racer racer = race.getRacer(player. getUniqueId());
            if (racer != null) {
                // Remove minecart
                if (racer.getMinecart() != null) {
                    racer.getMinecart().remove();
                }

                // Teleport to hub
                if (race.getTrack(). getHub() != null) {
                    player.teleport(race.getTrack().getHub());
                }

                // Restore player data
                racer.restoreInventory();

                // Remove from race
                race.removeRacer(racer.getPlayerUUID());

                Message.send(player, "&4left-race");

                // Check if race should end
                if (race.getRacers().isEmpty()) {
                    cancelRace(race. getTrack());
                }
            }
        }
    }

    public void cancelRace(Track track) {
        Race race = activeRaces.remove(track);
        if (race != null) {
            // Cancel tasks
            if (race.getUpdateTask() != null) {
                race.getUpdateTask().cancel();
            }

            // Restore all players
            for (Racer racer : race.getRacers()) {
                Player player = racer.getPlayer();
                if (player != null) {
                    if (racer.getMinecart() != null) {
                        racer.getMinecart().remove();
                    }

                    if (track.getHub() != null) {
                        player.teleport(track.getHub());
                    }

                    racer.restoreInventory();
                    Message.send(player, "race-cancelled");
                }

                playerRaces.remove(racer.getPlayerUUID());
            }
        }
    }

    private void updateLeaderboard(Race race) {
        for (Racer racer : race.getRacers()) {
            if (racer.isFinished()) {
                Track track = race.getTrack();

                // Add to fastest times if it qualifies
                if (track.isLeaderboardTime(racer.getFinishTime())) {
                    LeaderboardEntry entry = new LeaderboardEntry(
                            racer.getPlayerUUID(),
                            racer.getPlayer().getName(),
                            racer. getFinishTime()
                    );
                    track.addFastestTime(entry);
                }
            }
        }

        // Save leaderboard
        plugin.getDataManager().saveLeaderboard();

        // Update leaderboard displays
        plugin.getLeaderBoardManager().updateLeaderBoard(race.getTrack());
    }

    private void showResults(Race race) {
        List<Racer> finishedRacers = race.getRacers().stream()
                . filter(Racer::isFinished)
                .sorted(Comparator.comparingInt(Racer::getPosition))
                .toList();

        for (Racer racer : race.getRacers()) {
            Player player = racer.getPlayer();
            if (player != null) {
                player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                player.sendMessage("§e§lRace Resultaten - " + race.getTrack().getName());
                player.sendMessage("");

                int pos = 1;
                for (Racer r : finishedRacers) {
                    String posString = getPositionColor(pos) + pos + ". §f" + r.getPlayer().getName();
                    String timeString = "§e" + Time.formatTime(r.getFinishTime());
                    player.sendMessage(posString + " §7- " + timeString);
                    pos++;
                }

                player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            }
        }
    }

    private void giveRewards(Player player, Racer racer) {
        int coins = plugin.getConfig().getInt("rewards.coins-per-completion", 2);

        // TODO: Integrate with economy plugin (Vault)

        List<String> commands = plugin.getConfig().getStringList("rewards.custom-commands");
        for (String command : commands) {
            command = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        if (racer.getPosition() == 1) {
            List<String> winnerCommands = plugin.getConfig().getStringList("rewards.winner-commands");
            for (String command : winnerCommands) {
                command = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }

    private String getPositionColor(int position) {
        return switch (position) {
            case 1 -> "§6§l";
            case 2 -> "§7§l";
            case 3 -> "§c§l";
            default -> "§f";
        };
    }

    private static class PlayerData {
        ItemStack[] inventory;
        ItemStack[] armor;
        GameMode gameMode;
        Location location;
        int foodLevel;
        double health;
    }
}

