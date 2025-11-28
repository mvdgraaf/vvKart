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
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
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

                if (!(player.getVehicle() instanceof Minecart cart)) return;

                Race race = Main.getInstance().getRaceManager().getRace(player);
                if (race == null) return;

                if (race.getState() != Race.RaceState.RACING) {
                    cart.setVelocity(new Vector(0, cart.getVelocity().getY(), 0));
                    return;
                }

                Racer racer = race.getRacer(player);
                if (racer == null || racer.isFinished()) return;

                // Read WASD input
                float forward = event.getPacket().getFloat().read(1);   // W/S
                float sideways = event.getPacket().getFloat().read(0);  // A/D

                // Get player's horizontal look direction
                Vector dir = player.getLocation().getDirection().clone();
                dir.setY(0);
                if (dir.lengthSquared() == 0) dir = new Vector(0, 0, 1);
                dir.normalize();

                // Sideways vector
                Vector sideVec = new Vector(-dir.getZ(), 0, dir.getX()).normalize();

                // ========== CONFIGUREERBARE SETTINGS ==========
                double baseSpeed = plugin.getConfig().getDouble("kart.speed-multiplier", 1.0);
                double strafeMultiplier = plugin.getConfig().getDouble("kart.strafe-multiplier", 0.6);
                double acceleration = plugin.getConfig().getDouble("kart.acceleration", 0.3);
                double friction = plugin.getConfig().getDouble("kart.friction", 0.85);
                double maxSpeed = plugin.getConfig().getDouble("kart.max-speed", 2.0);
                double climbBoost = plugin.getConfig().getDouble("kart.climb-boost", 0.5);
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
                    newXZ.normalize().multiply(maxSpeed);
                }
                // ======================================

                // ========== IMPROVED CLIMB SYSTEM ==========
                Location cartLoc = cart.getLocation();
                boolean onGround = cart.isOnGround();

                if (newXZ.lengthSquared() > 0.01 && onGround) {
                    Vector moveDir = newXZ.clone().normalize();

                    // Check 0.4 blocks ahead
                    Location aheadLoc = cartLoc.clone().add(moveDir.multiply(0.4));

                    // Get blocks at different heights
                    Block blockAtFeet = aheadLoc.getBlock();
                    Block blockBelow = aheadLoc.clone().subtract(0, 0.5, 0).getBlock();
                    Block blockAbove = aheadLoc.clone().add(0, 1, 0).getBlock();

                    // Check if blocks are solid
                    boolean solidAtFeet = blockAtFeet.getType().isSolid();
                    boolean solidBelow = blockBelow.getType().isSolid();
                    boolean airAbove = !blockAbove.getType().isSolid();

                    // Check for slabs, stairs, carpet
                    boolean isPartialBlock = isPartialBlock(blockAtFeet) || isPartialBlock(blockBelow);

                    // Scenario 1: Full block at feet (1 block step)
                    if (solidAtFeet && airAbove) {
                        currentY = climbBoost;
                    }
                    // Scenario 2: Partial blocks (slabs, stairs, carpet)
                    else if (isPartialBlock && airAbove) {
                        currentY = climbBoost * 0.6;  // Smaller boost
                    }
                    // Scenario 3: Going uphill (block Y increases)
                    else if (solidBelow && aheadLoc.getBlockY() > cartLoc.getBlockY()) {
                        currentY = climbBoost * 0.8;
                    }
                    // Scenario 4: On flat ground
                    else {
                        currentY = -0.1;  // Stick to ground
                    }
                } else if (onGround) {
                    // Not moving or stationary
                    currentY = -0.1;
                } else {
                    // In air - apply gravity
                    currentY = Math.max(currentY - 0.08, -1.5);
                }
                // ===========================================

                // Final velocity
                Vector finalVel = new Vector(newXZ.getX(), currentY, newXZ.getZ());

                cart.setMaxSpeed(100);
                cart.setSlowWhenEmpty(false);
                cart.setVelocity(finalVel);

                // Rotation options
                if (rotateToMovement && newXZ.lengthSquared() > 0.01) {
                    // Rotate cart to movement direction
                    Location lookLoc = cart.getLocation();
                    Vector lookDir = newXZ.clone().normalize();
                    lookLoc.setDirection(lookDir);
                    cart.teleport(lookLoc);
                } else {
                    // Rotate cart to player look direction
                    cart.setRotation(player.getLocation().getYaw(), 0);
                }
            }

            // Helper method to detect partial blocks
            private boolean isPartialBlock(Block block) {
                if (block == null || !block.getType().isSolid()) return false;

                // Check for slabs
                if (block.getBlockData() instanceof Slab) {
                    return true;
                }

                // Check for stairs
                if (block.getBlockData() instanceof Stairs) {
                    return true;
                }

                // Check for carpet and other partial blocks
                return switch (block.getType()) {
                    case WHITE_CARPET, ORANGE_CARPET, MAGENTA_CARPET,
                         LIGHT_BLUE_CARPET, YELLOW_CARPET, LIME_CARPET, PINK_CARPET,
                         GRAY_CARPET, LIGHT_GRAY_CARPET, CYAN_CARPET, PURPLE_CARPET,
                         BLUE_CARPET, BROWN_CARPET, GREEN_CARPET, RED_CARPET, BLACK_CARPET,
                         SNOW, REPEATER, COMPARATOR, REDSTONE_WIRE,
                         LIGHT_WEIGHTED_PRESSURE_PLATE, HEAVY_WEIGHTED_PRESSURE_PLATE -> true;
                    default -> false;
                };
            }
        });

        plugin.getLogger().info("KartController registered with ProtocolLib!");
    }

    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
    }
}