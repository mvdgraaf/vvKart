package me.Codex. vvKart.Listeners;

import me.Codex. vvKart.Main;
import org.bukkit.Material;
import org.bukkit. entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event. Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private final Main plugin;

    public PlayerInteractListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || ! item.hasItemMeta()) return;

        // Check if it's the leave item
        String leaveItemName = plugin.getConfig().getString("leave-item.name", "&c&lVerlaat Race");
        leaveItemName = leaveItemName.replace("&", "§");

        if (item.getItemMeta().getDisplayName().equals(leaveItemName)) {
            event.setCancelled(true);

            // Check if player is in race
            if (plugin. getRaceManager().isInRace(player)) {
                plugin.getRaceManager().removeFromRace(player);
            }
        }
    }
}