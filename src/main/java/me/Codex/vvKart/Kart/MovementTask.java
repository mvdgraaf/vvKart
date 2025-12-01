package me.Codex.vvKart.Kart;

import me.Codex.vvKart.Main;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MovementTask {

    public void startMovementTask(Main plugin, Player player, ArmorStand kart) {
        new BukkitRunnable() {
            @Override
            public void run() {

                // Cancel task if kart or player is gone
                if (!kart.isValid() || !player.isOnline()) {
                    cancel();
                    return;
                }

                KartInput input = KartProtocolLib.inputs.get(player);
                if (input == null) return;

                float forward = input.forward;
                float sideways = input.sideways;

                double speed = 0.40;
                double hoverY = 0.03;

                // Direction the player is facing
                Location ploc = player.getLocation();
                Vector direction = ploc.getDirection().clone();
                direction.setY(0).normalize();

                Vector side = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

                // Base hover velocity
                Vector velocity = new Vector(0, hoverY, 0);

                // Movement input
                velocity.add(direction.clone().multiply(forward * speed));
                velocity.add(side.clone().multiply(sideways * speed * 0.6));

                // ========= HILL SYSTEM (UPHILL + DOWNHILL) =========
                if (velocity.lengthSquared() > 0.0001) {

                    Location kLoc = kart.getLocation();
                    Vector moveDir = velocity.clone().setY(0);

                    if (moveDir.lengthSquared() > 0.0001) {
                        moveDir.normalize();

                        // Position directly in front of kart
                        Location front = kLoc.clone().add(moveDir.clone().multiply(0.6));
                        Location frontBase = front.clone().add(0, -0.1, 0);
                        Location frontBelow = front.clone().add(0, -1, 0);
                        Location frontAbove = front.clone().add(0, 1, 0);

                        boolean blockInFront = front.getBlock().getType().isSolid();
                        boolean airAbove = frontAbove.getBlock().isPassable();

                        // ----- UPHILL -----
                        if (blockInFront && airAbove) {

                            double step = 0;

                            // Try half block (slab)
                            if (front.clone().add(0, 0.5, 0).getBlock().isPassable()) {
                                step = 0.5;
                            }
                            // Try full block (normal hill)
                            else if (front.clone().add(0, 1, 0).getBlock().isPassable()) {
                                step = 1.0;
                            }

                            if (step > 0) {
                                kLoc.add(0, step, 0);
                                kart.teleport(kLoc);
                                velocity.setY(0.15); // lift slightly to prevent snapping down
                            }
                        }

                        // ----- DOWNHILL -----
                        else if (
                                frontBase.getBlock().isPassable() &&   // front at same height is air
                                        frontBelow.getBlock().isPassable()      // lower level open
                        ) {
                            // Smooth glide downward
                            velocity.setY(-0.25);
                        }
                    }
                }
                // ========= END HILL SYSTEM =========

                // Apply velocity
                kart.setVelocity(velocity);

                // Rotate kart toward movement direction
                if (velocity.lengthSquared() > 0.001) {
                    Location newLocation = kart.getLocation();
                    newLocation.setDirection(velocity.clone().setY(0));
                    kart.teleport(newLocation);
                }
            }

        }.runTaskTimer(plugin, 1, 1);
    }
}
