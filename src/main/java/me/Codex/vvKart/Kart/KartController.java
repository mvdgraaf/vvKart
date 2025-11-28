package me.Codex.vvKart.Kart;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.Race;
import me.Codex.vvKart.Models.Racer;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KartController {

    private final Main plugin;

    public KartController(Main plugin) {
        this.plugin = plugin;
    }

    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.STEER_VEHICLE
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();

                // Check if player is in a minecart
                if (!(player.getVehicle() instanceof Minecart cart)) return;

                // Check if player is in a race
                Race race = Main.getInstance().getRaceManager().getRace(player);
                if (race == null) return;

                // Check race state
                if (race.getState() != Race.RaceState.RACING) {
                    // During countdown, don't allow movement
                    cart.setVelocity(new Vector(0, cart.getVelocity().getY(), 0));
                    return;
                }

                Racer racer = race.getRacer(player);
                if (racer == null || racer.isFinished()) return;

                // Read WASD input
                float forward = event.getPacket().getFloat().read(0);   // W/S
                float sideways = event.getPacket().getFloat().read(1);  // A/D

                // Some clients/protocols report forward inverted; flip if behavior is reversed.
                // If forward feels backwards in your tests, keep the negation. If it's correct, remove the -.
                forward = -forward; // invert forward so W -> positive forward motion

                // Optional: invert sideways if A/D reversed
                // sideways = -sideways;

                // Get player's horizontal look direction
                Vector dir = player.getLocation().getDirection().clone();
                dir.setY(0);
                if (dir.lengthSquared() == 0) dir = new Vector(0, 0, 1); // fallback
                dir.normalize();

                // Sideways vector (perpendicular on XZ plane)
                Vector sideVec = new Vector(-dir.getZ(), 0, dir.getX()).normalize();

                // Config / tuning
                double baseSpeed = plugin.getConfig().getDouble("race.speed-multiplier", 0.5);
                double strafeMultiplier = 0.6;         // A/D slower than forward
                double accel = 1.2;                    // responsiveness
                double friction = 0.92;                // damping per packet

                // Current vel and target vel
                Vector current = cart.getVelocity().clone();
                // preserve vertical for now (we'll override if needed)
                double currentY = current.getY();

                Vector inputVel = new Vector(0, 0, 0);

                // Forward/back
                if (Math.abs(forward) > 0.01f) {
                    inputVel.add(dir.clone().multiply(forward * baseSpeed));
                }

                // Strafe (A/D)
                if (Math.abs(sideways) > 0.01f) {
                    inputVel.add(sideVec.clone().multiply(sideways * baseSpeed * strafeMultiplier));
                }

                // Make target velocity (XZ)
                Vector targetXZ = new Vector(inputVel.getX(), 0, inputVel.getZ());

                // Apply simple accel: move current XZ toward targetXZ
                Vector currentXZ = new Vector(current.getX(), 0, current.getZ());
                Vector newXZ = currentXZ.multiply(friction).add(targetXZ.multiply(accel * (1.0 - friction)));

                // Helling/step-up detection:
                // look slightly ahead in movement direction and compare block Y
                Location cartLoc = cart.getLocation();
                Vector moveDir = newXZ.clone();
                if (moveDir.lengthSquared() > 0.0001) {
                    moveDir.normalize();
                    Location ahead = cartLoc.clone().add(moveDir.multiply(0.6)); // 0.6 block ahead
                    double aheadBlockY = ahead.getBlockY();
                    double cartBlockY = cartLoc.getBlockY();

                    // if ahead block is higher, give small upward velocity to climb
                    if (aheadBlockY > cartBlockY) {
                        currentY = 0.25; // tweak to taste
                    } else {
                        // keep existing vertical velocity (gravity) but slightly damp it if on ground
                        if (cart.isOnGround()) currentY = 0.0;
                    }
                }

                // set final velocity
                Vector finalVel = new Vector(newXZ.getX(), currentY, newXZ.getZ());

                cart.setMaxSpeed(100);
                cart.setSlowWhenEmpty(false);
                cart.setVelocity(finalVel);

                // Rotate minecart to face movement direction if moving
                if (newXZ.lengthSquared() > 0.0001) {
                    Location lookLoc = cart.getLocation();
                    Vector lookDir = new Vector(newXZ.getX(), 0, newXZ.getZ()).normalize();
                    // set a small forward offset to compute yaw
                    lookLoc.setDirection(lookDir);
                    cart.teleport(lookLoc);
                }
            }
        });

        plugin.getLogger().info("KartController registered with ProtocolLib!");
    }

    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
    }
}
