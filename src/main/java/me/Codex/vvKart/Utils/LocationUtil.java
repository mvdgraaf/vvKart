package me.Codex.vvKart.Utils;

import me.Codex.vvKart.Main;
import org.bukkit. Bukkit;
import org.bukkit.Location;
import org. bukkit.World;

public class LocationUtil {

    /**
     * Serialize a location to a string
     * Format: world,x,y,z,yaw,pitch
     */
    public static String serialize(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    /**
     * Deserialize a location from a string
     */
    public static Location deserialize(String string) {
        if (string == null || string.isEmpty()) {
            Main.getInstance().getLogger().warning("LocationUtil: string is null or empty");
            return null;
        }

        try {
            String[] parts = string.split(",");

            if (parts.length < 4) {
                Main.getInstance(). getLogger().warning("LocationUtil: Invalid format - " + string);
                return null;
            }

            World world = Bukkit.getWorld(parts[0]);

            if (world == null) {
                Main.getInstance().getLogger().warning("LocationUtil: World not found - " + parts[0]);
                return null;
            }

            double x = Double.parseDouble(parts[1]);
            double y = Double. parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);

            float yaw = 0;
            float pitch = 0;

            if (parts.length >= 5) {
                yaw = Float.parseFloat(parts[4]);
            }
            if (parts.length >= 6) {
                pitch = Float.parseFloat(parts[5]);
            }

            Location loc = new Location(world, x, y, z, yaw, pitch);
            Main.getInstance().getLogger().info("LocationUtil: Loaded location - " + loc);
            return loc;

        } catch (Exception e) {
            Main.getInstance().getLogger().warning("LocationUtil: Error deserializing - " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if a location is inside a bounding box
     */
    public static boolean isInside(Location location, Location pos1, Location pos2) {
        if (location == null || pos1 == null || pos2 == null) {
            return false;
        }

        if (! location.getWorld().equals(pos1.getWorld())) {
            return false;
        }

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math. min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return location.getX() >= minX && location. getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location. getZ() <= maxZ;
    }

    /**
     * Get distance between two locations (2D, ignoring Y)
     */
    public static double getDistance2D(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || ! loc1.getWorld().equals(loc2.getWorld())) {
            return Double.MAX_VALUE;
        }

        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();

        return Math.sqrt(dx * dx + dz * dz);
    }
}