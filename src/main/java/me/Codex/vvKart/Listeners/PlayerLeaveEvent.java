package me.Codex.vvKart.Listeners;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Manages.RaceManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveEvent implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        RaceManager raceManager = Main.getInstance().getRaceManager();
        if (raceManager.isInRace(player)) {
            raceManager.removeFromRace(player);
        }
    }
}
