package me.Codex.vvKart.Kart;

import me.Codex.vvKart.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Kart {

    private final Main plugin;
    private final static Map<Player, ArmorStand> karts = new HashMap<>();

    public Kart(Main plugin) {
        this.plugin = plugin;
    }

    public void spawnKart(Player player) {

        Location location = player.getLocation();

        MovementTask task = new MovementTask();

        ArmorStand armorStand = player.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setCustomName(player.getName() + "_kart");
            stand.setCustomNameVisible(false);
        });

        armorStand.getEquipment().setHelmet(new ItemStack(Material.IRON_BLOCK));

        armorStand.addPassenger(player);

        karts.put(player, armorStand);

        KartProtocolLib.registerPlayer(player);
        task.startMovementTask(plugin, player, armorStand);

    }

    public static void despawnKart(Player player) {
        ArmorStand armorStand = karts.get(player);
        armorStand.remove();
        karts.remove(player);
    }

}
