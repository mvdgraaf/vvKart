package me.Codex.vvKart.Models;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class Checkpoint {

    private final int number;
    private final Location point1;
    private final Location point2;
    private final BoundingBox boundingBox;

    public Checkpoint(int number, Location point1, Location point2) {
        this.number = number;
        this.point1 = point1;
        this.point2 = point2;

        this.boundingBox = createBoundingBox(point1, point2);
    }

    public BoundingBox createBoundingBox(Location loc1, Location loc2) {
        double minX = Math.min(loc1.getX(), loc2.getX());
        double minY = Math.min(loc1.getY(), loc2.getY());
        double minZ = Math.min(loc1.getZ(), loc2.getZ());

        double maxX = Math.max(loc1.getX(), loc2.getX());
        double maxY = Math.max(loc1.getY(), loc2. getY());
        double maxZ = Math.max(loc1. getZ(), loc2.getZ());

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean isInside(Location location) {
        if(!location.getWorld().equals(point1.getWorld())) return false;
        return boundingBox.contains(location. getX(), location.getY(), location.getZ());
    }

    public boolean overlaps(Location location) {
        if(!location.getWorld().equals(point1.getWorld())) return false;
        double radius = 0.3;
        BoundingBox playerBox = new BoundingBox(
                location. getX() - radius,
                location.getY(),
                location.getZ() - radius,
                location.getX() + radius,
                location. getY() + 1.8,
                location.getZ() + radius
        );

        return boundingBox.overlaps(playerBox);
    }

    public int getNumber() {
        return number;
    }

    public Location getPoint1() {
        return point1.clone();
    }

    public Location getPoint2() {
        return point2.clone();
    }

    public BoundingBox getBoundingBox() {
        return boundingBox.clone();
    }

    public Location getCenter() {
        double x = (point1.getX() + point2.getX()) / 2;
        double y = (point1.getY() + point2.getY()) / 2;
        double z = (point1.getZ() + point2.getZ()) / 2;

        return new Location(point1.getWorld(), x, y, z);
    }

    @Override
    public String toString() {
        return "Checkpoint{" +
                "number=" + number +
                ", world=" + point1.getWorld(). getName() +
                ", from=[" + (int)point1.getX() + "," + (int)point1.getY() + "," + (int)point1.getZ() + "]" +
                ", to=[" + (int)point2.getX() + "," + (int)point2. getY() + "," + (int)point2.getZ() + "]" +
                '}';
    }

    public double getVolume() {
        return boundingBox.getVolume();
    }

    public double getWidth() {
        return boundingBox.getWidthX();
    }

    public double getHeight() {
        return boundingBox.getHeight();
    }

    public double getLength() {
        return boundingBox.getWidthZ();
    }
}
