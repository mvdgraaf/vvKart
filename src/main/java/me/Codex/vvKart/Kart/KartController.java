package me.Codex.vvKart.Kart;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol. ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.Race;
import me.Codex.vvKart.Models. Racer;
import org. bukkit.entity.Minecart;
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
                ListenerPriority. NORMAL,
                PacketType. Play.Client.STEER_VEHICLE
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

                // Get WASD input
                float forward = event.getPacket().getFloat().read(0);   // W/S (-1. 0 to 1.0)
                float sideways = event. getPacket().getFloat().read(1);  // A/D (-1. 0 to 1.0)

                // Get player's look direction (for steering)
                Vector direction = player.getLocation().getDirection();
                direction.setY(0);  // No vertical movement
                direction.normalize();

                // Get sideways direction (perpendicular to look direction)
                Vector sidewaysVector = new Vector(-direction.getZ(), 0, direction.getX()). normalize();

                // Get speed from config
                double baseSpeed = plugin.getConfig().getDouble("race.speed-multiplier", 0.5);

                // Apply speed boost if configured (e.g., from items)
                double speedMultiplier = 1.0;
                // TODO: Add speed boost from power-ups here

                double finalSpeed = baseSpeed * speedMultiplier;

                // Calculate velocity
                Vector velocity = new Vector(0, 0, 0);

                // Forward/backward movement
                if (Math.abs(forward) > 0.01) {
                    velocity.add(direction.clone().multiply(forward * finalSpeed));
                }

                // Strafe movement (A/D keys)
                if (Math.abs(sideways) > 0.01) {
                    velocity. add(sidewaysVector.clone(). multiply(sideways * finalSpeed * 0.5)); // Strafe slower
                }

                // Preserve Y velocity (gravity/jumping)
                velocity.setY(cart.getVelocity().getY());

                // Apply slight downward force if in air (better ground contact)
                if (! cart.isOnGround()) {
                    velocity.setY(velocity.getY() - 0.08);
                }

                // Set minecart properties for smoother movement
                cart.setMaxSpeed(100);  // Remove vanilla speed limit
                cart.setSlowWhenEmpty(false);
                cart.setVelocity(velocity);

                // Rotate minecart to match player's view
                cart.setRotation(player.getLocation().getYaw(), 0);
            }
        });

        plugin.getLogger().info("KartController registered with ProtocolLib!");
    }

    public void unregister() {
        ProtocolLibrary. getProtocolManager().removePacketListeners(plugin);
    }
}