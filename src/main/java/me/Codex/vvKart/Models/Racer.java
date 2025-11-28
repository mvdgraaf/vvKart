package me.Codex.vvKart.Models;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Utils.Message;
import me.Codex.vvKart.Utils.Time;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Racer {

    private final UUID playerUUID;
    private final Player player;
    private final Race race;

    private int currentLap;
    private int currentCheckpoint;
    private Set<Integer> passedCheckpoints;
    private int position;

    private long startTime;
    private long finishTime;
    private long lastLapTime;

    private ArmorStand armorStand;
    private Minecart minecart;

    private ItemStack[] inventoryContents;
    private ItemStack[] armorContents;
    private ItemStack offHandItem;

    private boolean finished;
    private boolean disqualified;

    public Racer(Player player, Race race) {
        this.playerUUID = player.getUniqueId();
        this.player = player;
        this.race = race;

        this.currentLap = 0;
        this.currentCheckpoint = 0;
        this.passedCheckpoints = new HashSet<>();
        this.position = 0;

        this.finished = false;
        this.disqualified = false;
    }

    public void saveInventory() {
        this.inventoryContents = player.getInventory().getContents().clone();
        this.armorContents = player.getInventory().getArmorContents().clone();
        this.offHandItem = player.getInventory().getItemInOffHand().clone();
    }

    public void restoreInventory() {
        if (inventoryContents != null) {
            player.getInventory().setContents(inventoryContents);
        }
        if (armorContents != null) {
            player.getInventory().setArmorContents(armorContents);
        }
        if (offHandItem != null) {
            player.getInventory().setItemInOffHand(offHandItem);
        }
    }

    public void clearInventory() {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
    }

    public void giveLeaveItem() {
        String materialName = Main.getInstance().getConfig().getString("leave-item.material", "RED_BED");
        Material material = Material.getMaterial(materialName);
        if (material == null) material = Material.RED_BED;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = Main.getInstance().getConfig().getString("leave-item.name", "&c&lVerlaat Race");
        meta.setDisplayName(Message.getMessage("leave-item.name"));

        List<String> lore = Main.getInstance().getConfig().getStringList("leave-item.lore");
        if (! lore.isEmpty()) {
            meta.setLore(lore. stream()
                    .map(line -> line.replace("&", "§"))
                    .toList());
        }

        item.setItemMeta(meta);

        int slot = Main.getInstance().getConfig().getInt("leave-item.slot", 8);
        player.getInventory().setItem(slot, item);
    }

    public void passCheckpoint(int checkPointNumber) {
        passedCheckpoints.add(checkPointNumber);
        currentCheckpoint = checkPointNumber;
    }

    public void completeLap() {
        currentLap++;
        passedCheckpoints.clear();
        currentCheckpoint = 0;
        lastLapTime = System.currentTimeMillis() - (finishTime > 0 ? finishTime : startTime);
    }

    public long getElapsedTime() {
        if (finished && finishTime > 0) {
            return finishTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    public String getFormattedTime() {
        return Time.formatTime(getElapsedTime());
    }
    // Getters and Setters
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Player getPlayer() {
        return player;
    }

    public Race getRace() {
        return race;
    }

    public int getCurrentLap() {
        return currentLap;
    }

    public void setCurrentLap(int currentLap) {
        this.currentLap = currentLap;
    }

    public int getCurrentCheckpoint() {
        return currentCheckpoint;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
        this.finished = true;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public void setArmorStand(ArmorStand armorStand) {
        this.armorStand = armorStand;
    }

    public Minecart getMinecart() {
        return minecart;
    }

    public void setMinecart(Minecart minecart) {
        this.minecart = minecart;
    }

    public void removeEntities() {
        if (minecart != null && minecart.isValid()) {
            minecart.remove();
        }
        if (armorStand != null && armorStand.isValid()) {
            armorStand.remove();
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isDisqualified() {
        return disqualified;
    }

    public void setDisqualified(boolean disqualified) {
        this.disqualified = disqualified;
    }

    public Set<Integer> getPassedCheckpoints() {
        return new HashSet<>(passedCheckpoints);
    }
}
