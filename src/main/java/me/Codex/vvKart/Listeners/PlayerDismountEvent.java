package me.Codex.vvKart.Listeners;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.Race;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class PlayerDismountEvent implements Listener {

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!KartProtocolLib.inputs.containsKey(player)) return; // niet in kart

        Race race = Main.getInstance().getRaceManager().getRace(player);
        if (race != null && race.getState() == Race.RaceState.RACING) {
            // Tijdens race blokkeren we dismount
            event.setCancelled(true);
            return;
        }

        // Buiten actieve race: laat dismount toe maar ruim kart op
        Kart.despawnKart(player);
    }
}
