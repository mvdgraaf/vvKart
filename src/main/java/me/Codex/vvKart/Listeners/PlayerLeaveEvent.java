package me.Codex.vvKart.Listeners;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Manages.RaceManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveEvent implements Listener {

    private final Main plugin;

    public PlayerLeaveEvent(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        RaceManager raceManager = Main.getInstance().getRaceManager();
        plugin.getQueueManager().removeFromQueue(player);
        if (raceManager.isInRace(player)) {
            raceManager.getRacer(player).restoreInventory();
            raceManager.removeFromRace(player);
        }
    }
}
