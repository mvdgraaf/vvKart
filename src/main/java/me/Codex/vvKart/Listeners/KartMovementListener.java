package me.Codex.vvKart.Listeners;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.Race;
import me.Codex.vvKart.Models.Racer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class KartMovementListener extends BukkitRunnable {

    private final Main plugin;

    public KartMovementListener(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
//        for (Player player : plugin.getServer().getOnlinePlayers()) {
//            Race race = plugin.getRaceManager().getRace(player);
//            if (race != null) continue;
//
//            Racer racer = plugin.getRaceManager().getRacer(player);
//            if (racer == null) { continue;}
//            if(racer.isFinished()) continue;
//
//            ArmorStand armorStand = racer.getArmorStand();
//
//            if (armorStand == null || !armorStand.isValid()) {
//                continue;
//            }
//
//            Vector direction = player.getLocation().getDirection();
//            direction.setY(0);
//            direction.normalize();
//
//            double speed = plugin.getConfig().getDouble("race.speed-multiplier");
//
//            Vector velocity = direction.multiply(speed);
//            velocity.setY(armorStand.getVelocity().getY());
//
//            armorStand.setVelocity(velocity);
//
//            armorStand.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());
//
//            if (player.getVehicle() != armorStand) {
//                armorStand.addPassenger(player);
//            }
//        }
  }
}
