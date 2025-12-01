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

@Deprecated
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

                if (!(player.getVehicle() instanceof Minecart cart)) return;

                Race race = Main.getInstance().getRaceManager().getRace(player);

                if (race.getState() != Race.RaceState.RACING) {
                    cart.setVelocity(new Vector(0, cart.getVelocity().getY(), 0));
                    return;
                }

                Racer racer = race.getRacer(player);
                if (racer == null || racer.isFinished()) return;

                // Read WASD input
                float forward = event.getPacket().getFloat().read(1);
                float sideways = event.getPacket().getFloat().read(0);

                // Player forward direction (XZ only)
                Vector dir = player.getLocation().getDirection().clone();
                dir.setY(0);
                if (dir.lengthSquared() == 0) dir = new Vector(0, 0, 1);
                dir.normalize();

                // Sideways vector
                Vector sideVec = new Vector(-dir.getZ(), 0, dir.getX()).normalize();

                // Config values
                double baseSpeed = plugin.getConfig().getDouble("kart.speed-multiplier", 1.0);
                double strafeMultiplier = plugin.getConfig().getDouble("kart.strafe-multiplier", 0.6);
                double acceleration = plugin.getConfig().getDouble("kart.acceleration", 0.3);
                double friction = plugin.getConfig().getDouble("kart.friction", 0.85);
                double maxSpeed = plugin.getConfig().getDouble("kart.max-speed", 2.0);
                boolean rotateToMovement = plugin.getConfig().getBoolean("kart.rotate-to-movement", false);

                Vector current = cart.getVelocity().clone();
                double currentY = current.getY();

                Vector inputVel = new Vector(0, 0, 0);

                if (Math.abs(forward) > 0.01f)
                    inputVel.add(dir.clone().multiply(forward * baseSpeed));

                if (Math.abs(sideways) > 0.01f)
                    inputVel.add(sideVec.clone().multiply(sideways * baseSpeed * strafeMultiplier));

                Vector currentXZ = new Vector(current.getX(), 0, current.getZ());
                Vector targetXZ = new Vector(inputVel.getX(), 0, inputVel.getZ());

                Vector newXZ;

                if (targetXZ.lengthSquared() > 0.0001) {
                    newXZ = currentXZ.multiply(friction).add(targetXZ.multiply(acceleration));
                } else {
                    newXZ = currentXZ.multiply(friction);
                }

                if (newXZ.length() > maxSpeed)
                    newXZ = newXZ.normalize().multiply(maxSpeed);

                Location cartLoc = cart.getLocation();
                Vector moveDir = newXZ.clone().normalize();

                // ===========================================================
                // REAL STEP-UP SYSTEM → Kart rijdt nu ECHT hellingen op
                // ===========================================================
                if (cart.isOnGround() && newXZ.lengthSquared() > 0.001) {

                    Location front = cartLoc.clone().add(moveDir.clone().multiply(0.8));

                    boolean blockInFront = front.getBlock().getType().isSolid();
                    boolean airAbove = front.clone().add(0, 1, 0).getBlock().getType().isAir();

                    if (blockInFront && airAbove) {
                        // Tilt the cart up one block
                        cart.teleport(cartLoc.clone().add(0, 0.65, 0));

                        // Small upward correction
                        currentY = 0.05;
                    }
                }

                // Ground friction Y
                if (cart.isOnGround()) {
                    currentY = -0.05;
                } else {
                    // Gravity
                    currentY = Math.max(currentY - 0.08, -1.5);
                }

                Vector finalVel = new Vector(newXZ.getX(), currentY, newXZ.getZ());
                cart.setMaxSpeed(100);
                cart.setSlowWhenEmpty(false);
                cart.setVelocity(finalVel);

                // Rotation
                if (rotateToMovement && newXZ.lengthSquared() > 0.01) {
                    Location lookLoc = cart.getLocation();
                    lookLoc.setDirection(newXZ.clone().normalize());
                    cart.teleport(lookLoc);
                } else {
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
