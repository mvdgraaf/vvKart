package me.Codex.vvKart.Listeners;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.Track;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check for leave item usage
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item != null && item.hasItemMeta()) {
                String leaveMaterialName = Main.getInstance().getConfig().getString("leave-item. material", "RED_BED");
                Material leaveMaterial = Material.getMaterial(leaveMaterialName);

                if (leaveMaterial != null && item.getType() == leaveMaterial) {
                    // Check if player is in race
                    if (Main.getInstance().getRaceManager().isInRace(player)) {
                        event. setCancelled(true);
                        Main.getInstance().getRaceManager().removeFromRace(player);
                        return;
                    }
                }
            }
        }

        // Check for track join (clicking finish line)
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        // Check if player clicked on a track's finish line
        Track track = Main.getInstance().getTrackManager().getTrackAtLocation(event.getClickedBlock().getLocation());

        if (track != null) {
            event.setCancelled(true);
            Main.getInstance().getQueueManager(). addToQueue(player, track);
        }
    }
}
