package me.Codex.vvKart.Events;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.Racer;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class MinecraftControlListener implements Listener {

    private final Main plugin;
    private static final double SPEED = 0.5;
    private static final double MAX_SPEED = 1.0;

    public MinecraftControlListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (!plugin.getRaceManager().isInRace(player)) return;

        if(!(player.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        Racer racer = plugin.getRaceManager().getRacer(player);
        if(racer == null || racer.isFinished()) return;

        Vector direction = player.getLocation().getDirection();
        //direction.setY(0);
        direction.normalize();

        Vector velocity = minecart.getVelocity();

        if(player.isSneaking()) {
            velocity = direction.clone().multiply(-SPEED * 0.5);
        } else {
            velocity = direction.clone().multiply(SPEED);
        }

        double speedMultiplier = plugin.getConfig().getDouble("speed-multiplier");
        velocity.multiply(speedMultiplier);

        if(velocity.length() > MAX_SPEED) {
            velocity.normalize().multiply(MAX_SPEED);
        }

        velocity.setY(minecart.getVelocity().getY());

        minecart.setVelocity(velocity);
    }
}
