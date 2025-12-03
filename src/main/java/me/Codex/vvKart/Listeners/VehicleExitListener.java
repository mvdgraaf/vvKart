package me.Codex.vvKart.Listeners;

import me.Codex.vvKart.Main;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VehicleExitListener implements Listener {

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
        // Voorkom dat spelers van de pig (kart) afgaan tijdens een race
        if (e.getVehicle() instanceof Pig pig) {
            if (e.getExited() instanceof Player player) {
                if (Main.getInstance().getRaceManager().isInRace(player)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
