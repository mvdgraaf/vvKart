package me.Codex.vvKart.Events;

import me.Codex.vvKart.Main;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VehicleExitListener implements Listener {

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
        if (e.getVehicle() instanceof Minecart minecart) {
            if (e.getExited() instanceof Player player) {
                if (Main.getInstance().getRaceManager().isInRace(player)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
