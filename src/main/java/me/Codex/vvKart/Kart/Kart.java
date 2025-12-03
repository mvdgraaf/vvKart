package me.Codex.vvKart.Kart;

import me.Codex.vvKart.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Kart {
    private final Main plugin;
    private final static Map<Player, Pig> pigMap = new HashMap<>();
    private final static Map<Player, ArmorStand> visualMap = new HashMap<>();
    private final static Map<Player, KartStats> statsMap = new HashMap<>();

    public Kart(Main plugin) { this.plugin = plugin; }

    // Inner class voor kart statistieken
    public static class KartStats {
        public double maxSpeed = 3.5;
        public double acceleration = 0.4;
        public double friction = 0.92;
        public double turnSpeed = 3.5;
        public double currentSpeed = 0.0;
        public double speedMultiplier = 2.0;

        // Voor toekomstige features
        public double driftFactor = 1.0;
        public boolean isDrifting = false;
        public long lastTurnTime = 0;
    }

    public void spawnKart(Player player) {
        Location loc = player.getLocation();

        // Spawn invisible pig (het voertuig dat we besturen)
        Pig pig = player.getWorld().spawn(loc, Pig.class, p -> {
            p.setAI(false);
            p.setAdult();
            p.setSaddle(true); // nodig voor stuurcontrole
            p.setInvulnerable(true);
            p.setSilent(true);
            p.setCollidable(false);
            p.setInvisible(true);
        });

        // Spawn invisible armor stand (voor toekomstige custom model data)
        ArmorStand visual = player.getWorld().spawn(loc, ArmorStand.class, a -> {
            a.setInvulnerable(true);
            a.setGravity(false);
            a.setMarker(true);
            a.setVisible(false);
            a.setSmall(false);
        });
        // Placeholder model - vervang later met custom model data
        visual.getEquipment().setHelmet(new ItemStack(Material.IRON_BLOCK));

        // Zet speler op de pig
        pig.addPassenger(player);
        // Zet armor stand ook op de pig zodat deze meebeweegt
        pig.addPassenger(visual);

        pigMap.put(player, pig);
        visualMap.put(player, visual);

        // Initialiseer kart stats
        KartStats stats = new KartStats();
        statsMap.put(player, stats);

        // Start movement task
        MovementTask movementTask = new MovementTask();
        movementTask.start(plugin, player, pig, visual, stats);
    }

    public static void despawnKart(Player player) {
        Pig pig = pigMap.remove(player);
        ArmorStand visual = visualMap.remove(player);
        statsMap.remove(player);

        if (pig != null && pig.isValid()) {
            pig.eject(); // eject passengers
            pig.remove();
        }
        if (visual != null && visual.isValid()) {
            visual.remove();
        }
    }

    public static Pig getPig(Player player) { return pigMap.get(player); }
    public static ArmorStand getVisual(Player player) { return visualMap.get(player); }
    public static KartStats getStats(Player player) { return statsMap.get(player); }
}
