package me.Codex.vvKart.Listeners;

import me.Codex.vvKart.Kart.KartProtocolLib;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class PlayerDismountEvent implements Listener {

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;

        if(!KartProtocolLib.inputs.containsKey(player)) return;

        event.setCancelled(true);
    }


}
