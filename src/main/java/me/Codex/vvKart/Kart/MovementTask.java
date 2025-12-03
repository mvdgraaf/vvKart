package me.Codex.vvKart.Kart;

import me.Codex.vvKart.Main;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MovementTask {

    public void start(Main plugin, Player player, Pig pig, ArmorStand visual, Kart.KartStats stats) {
        // Basis movement speed van de pig
        pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.4);

        new BukkitRunnable() {
            private float lastYaw = pig.getLocation().getYaw();
            private int ticksSinceLastTurn = 0;
            private double currentVelocityMagnitude = 0.0;

            @Override
            public void run() {
                // Validatie checks
                if (!player.isOnline() || pig == null || !pig.isValid() || visual == null || !visual.isValid()) {
                    cancel();
                    return;
                }

                if (!player.isInsideVehicle() || player.getVehicle() == null || !player.getVehicle().equals(pig)) {
                    cancel();
                    return;
                }

                // === BOCHT DETECTIE ===
                float currentYaw = pig.getLocation().getYaw();
                float yawDelta = Math.abs(currentYaw - lastYaw);

                // Normaliseer yaw delta (360° wrap around)
                if (yawDelta > 180) {
                    yawDelta = 360 - yawDelta;
                }

                // Bocht detectie: Als pig meer dan 5° draait
                if (yawDelta > 5.0f) {
                    ticksSinceLastTurn = 0;
                    stats.isDrifting = true;
                    stats.lastTurnTime = System.currentTimeMillis();
                    // Snelheidsreductie in bochten (15% langzamer)
                    stats.driftFactor = 0.85;
                } else {
                    ticksSinceLastTurn++;
                    if (ticksSinceLastTurn > 10) {
                        stats.isDrifting = false;
                        stats.driftFactor = 1.0;
                    }
                }

                lastYaw = currentYaw;

                // === BEWEGING & ACCELERATIE ===
                Vector pigVelocity = pig.getVelocity();

                // Bereken huidige horizontale snelheid
                double horizontalSpeed = Math.sqrt(pigVelocity.getX() * pigVelocity.getX() +
                                                   pigVelocity.getZ() * pigVelocity.getZ());

                // Check of speler beweegt (pig's natural movement van WASD)
                boolean isMoving = horizontalSpeed > 0.01;

                if (isMoving) {
                    // === ACCELERATIE ===
                    // Target snelheid met drift factor
                    double targetSpeed = stats.maxSpeed * stats.driftFactor;

                    // Accelereer naar target speed
                    if (currentVelocityMagnitude < targetSpeed) {
                        currentVelocityMagnitude += stats.acceleration;
                        if (currentVelocityMagnitude > targetSpeed) {
                            currentVelocityMagnitude = targetSpeed;
                        }
                    } else if (currentVelocityMagnitude > targetSpeed) {
                        // Decelereer als we te snel gaan (bijv. na bocht)
                        currentVelocityMagnitude -= stats.acceleration * 0.5;
                    }

                    // Bereken richting van beweging
                    Vector direction = new Vector(pigVelocity.getX(), 0, pigVelocity.getZ()).normalize();

                    // Pas velocity toe met onze berekende snelheid en multiplier
                    double finalSpeed = currentVelocityMagnitude * stats.speedMultiplier;
                    pigVelocity.setX(direction.getX() * finalSpeed);
                    pigVelocity.setZ(direction.getZ() * finalSpeed);

                    // Update stats
                    stats.currentSpeed = currentVelocityMagnitude;

                } else {
                    // === DECELERATIE (W losgelaten) ===
                    if (currentVelocityMagnitude > 0) {
                        // Friction: langzamer worden
                        currentVelocityMagnitude *= stats.friction;

                        // Stop helemaal als te langzaam
                        if (currentVelocityMagnitude < 0.01) {
                            currentVelocityMagnitude = 0;
                            stats.currentSpeed = 0;
                        } else {
                            stats.currentSpeed = currentVelocityMagnitude;
                        }
                    }
                }

                // === GRAVITY & HILL CLIMBING ===
                Location pigLoc = pig.getLocation();

                // Check ground onder pig
                Location below = pigLoc.clone().subtract(0, 0.5, 0);
                boolean onGround = below.getBlock().getType().isSolid();

                if (onGround && horizontalSpeed > 0.05) {
                    // Bereken richting waar pig naartoe kijkt
                    Vector lookDirection = pigLoc.getDirection().setY(0).normalize();
                    Location frontCheck = pigLoc.clone().add(lookDirection.multiply(0.8));

                    // Check voor obstakels/heuvels
                    if (frontCheck.getBlock().getType().isSolid()) {
                        Location frontUp = frontCheck.clone().add(0, 1, 0);
                        Location frontHalfUp = frontCheck.clone().add(0, 0.5, 0);

                        // Hill climbing: spring omhoog als er ruimte is
                        if (frontUp.getBlock().isPassable()) {
                            // Volle block omhoog
                            double jumpStrength = 0.4 + (stats.currentSpeed / stats.maxSpeed) * 0.1;
                            pigVelocity.setY(jumpStrength);
                        } else if (frontHalfUp.getBlock().isPassable()) {
                            // Halve block omhoog (slabs etc)
                            pigVelocity.setY(0.3);
                        }
                    } else {
                        // Check voor downhill (naar beneden)
                        Location frontBelow = frontCheck.clone().subtract(0, 1, 0);
                        if (frontCheck.getBlock().isPassable() && !frontBelow.getBlock().getType().isSolid()) {
                            // Er is geen grond voor ons - ga naar beneden
                            if (pigVelocity.getY() > -0.5) {
                                pigVelocity.setY(-0.3);
                            }
                        }
                    }
                }

                // Pas finale velocity toe op pig
                pig.setVelocity(pigVelocity);

                // === ARMOR STAND VISUAL UPDATE ===
                Location visualLoc = pig.getLocation().clone();
                visualLoc.setYaw(pig.getLocation().getYaw());
                visualLoc.setPitch(0);
                visual.teleport(visualLoc);
            }
        }.runTaskTimer(plugin, 0L, 1L); // Elke tick (20x per seconde)
    }
}

