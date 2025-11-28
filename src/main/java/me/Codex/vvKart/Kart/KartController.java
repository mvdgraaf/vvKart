package me. Codex.vvKart. Kart;

import com. comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com. comphenix.protocol.events. PacketAdapter;
import com. comphenix.protocol.events. PacketEvent;
import me. Codex.vvKart.Main;
import me. Codex.vvKart. Models.Race;
import me. Codex.vvKart.Models.Racer;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit. util.Vector;

public class KartController {

    private final Main plugin;

    public KartController(Main plugin) {
        this.plugin = plugin;
    }

    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Client. STEER_VEHICLE
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();

                if (!(player.getVehicle() instanceof Minecart cart)) return;

                Race race = Main.getInstance().getRaceManager().getRace(player);

                if (race.getState() != Race.RaceState.RACING) {
                    cart.setVelocity(new Vector(0, cart.getVelocity().getY(), 0));
                    return;
                }

                Racer racer = race.getRacer(player);
                if (racer == null || racer.isFinished()) return;

                // Read WASD input
                float forward = event.getPacket().getFloat(). read(1);   // W/S
                float sideways = event.getPacket().getFloat(). read(0);  // A/D

                forward = -forward; // invert forward so W -> positive

                // Get player's horizontal look direction
                Vector dir = player.getLocation().getDirection().clone();
                dir. setY(0);
                if (dir.lengthSquared() == 0) dir = new Vector(0, 0, 1);
                dir.normalize();

                // Sideways vector
                Vector sideVec = new Vector(-dir.getZ(), 0, dir.getX()).normalize();

                // ========== CONFIGUREERBARE SETTINGS ==========
                double baseSpeed = plugin.getConfig(). getDouble("kart.speed-multiplier", 1.0);
                double strafeMultiplier = plugin.getConfig().getDouble("kart.strafe-multiplier", 0.6);
                double acceleration = plugin.getConfig().getDouble("kart.acceleration", 0.3);
                double friction = plugin. getConfig().getDouble("kart.friction", 0.85);
                double maxSpeed = plugin.getConfig().getDouble("kart. max-speed", 2.0);
                double climbBoost = plugin.getConfig().getDouble("kart.climb-boost", 0.3);
                boolean rotateToMovement = plugin.getConfig().getBoolean("kart.rotate-to-movement", false);
                // ==============================================

                // Current velocity
                Vector current = cart.getVelocity().clone();
                double currentY = current.getY();

                // Calculate target velocity from input
                Vector inputVel = new Vector(0, 0, 0);

                // Forward/backward
                if (Math.abs(forward) > 0.01f) {
                    inputVel.add(dir.clone().multiply(forward * baseSpeed));
                }

                // Strafe (A/D)
                if (Math.abs(sideways) > 0.01f) {
                    inputVel.add(sideVec.clone().multiply(sideways * baseSpeed * strafeMultiplier));
                }

                // Current XZ velocity
                Vector currentXZ = new Vector(current.getX(), 0, current.getZ());

                // Target XZ velocity
                Vector targetXZ = new Vector(inputVel.getX(), 0, inputVel.getZ());

                // ========== IMPROVED PHYSICS ==========
                // Apply acceleration toward target
                Vector newXZ;
                if (targetXZ.lengthSquared() > 0.0001) {
                    // Accelerating
                    newXZ = currentXZ.multiply(friction).add(targetXZ.multiply(acceleration));
                } else {
                    // No input - apply friction/deceleration
                    newXZ = currentXZ.multiply(friction);
                }

                // Clamp to max speed
                double currentSpeed = newXZ.length();
                if (currentSpeed > maxSpeed) {
                    newXZ. normalize(). multiply(maxSpeed);
                }
                // ======================================

                // Hill climbing detection
                Location cartLoc = cart.getLocation();
                Vector moveDir = newXZ.clone();
                if (moveDir.lengthSquared() > 0.0001) {
                    moveDir.normalize();
                    Location ahead = cartLoc.clone().add(moveDir.multiply(0.6));
                    double aheadBlockY = ahead.getBlockY();
                    double cartBlockY = cartLoc.getBlockY();

                    if (aheadBlockY > cartBlockY && cart.isOnGround()) {
                        // Climbing
                        currentY = climbBoost;
                    } else if (cart.isOnGround()) {
                        // On ground, reset Y
                        currentY = -0.05; // Slight downward for better ground contact
                    }
                } else if (cart.isOnGround()) {
                    currentY = -0.05;
                }

                // Apply gravity if in air
                if (!cart.isOnGround()) {
                    currentY = Math.max(currentY - 0.08, -1.5); // Terminal velocity
                }

                // Final velocity
                Vector finalVel = new Vector(newXZ.getX(), currentY, newXZ.getZ());

                cart. setMaxSpeed(100);
                cart.setSlowWhenEmpty(false);
                cart.setVelocity(finalVel);

                // Rotation options
                if (rotateToMovement && newXZ.lengthSquared() > 0.01) {
                    // Rotate cart to movement direction
                    Location lookLoc = cart.getLocation();
                    Vector lookDir = newXZ.clone(). normalize();
                    lookLoc.setDirection(lookDir);
                    cart.teleport(lookLoc);
                } else {
                    // Rotate cart to player look direction
                    cart.setRotation(player.getLocation().getYaw(), 0);
                }
            }
        });

        plugin.getLogger().info("KartController registered with ProtocolLib!");
    }

    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
    }
}