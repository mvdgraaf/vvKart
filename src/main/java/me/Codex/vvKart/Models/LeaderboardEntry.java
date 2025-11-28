package me.Codex.vvKart.Models;

import java.util. UUID;

public class LeaderboardEntry {

    private final UUID playerUUID;
    private final String playerName;
    private final long finishTime;  // Time in milliseconds
    private final long timestamp;   // When this record was set
    private int position;           // Current position in leaderboard (can change)

    /**
     * Constructor for creating new entry
     */
    public LeaderboardEntry(UUID playerUUID, String playerName, long finishTime) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.finishTime = finishTime;
        this.timestamp = System.currentTimeMillis();
        this.position = 0;
    }

    /**
     * Constructor for loading from file
     */
    public LeaderboardEntry(UUID playerUUID, String playerName, long finishTime, int position, long timestamp) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.finishTime = finishTime;
        this.timestamp = timestamp;
        this.position = position;
    }

    /**
     * Constructor with position (for race results)
     */
    public LeaderboardEntry(UUID playerUUID, String playerName, long finishTime, int position) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.finishTime = finishTime;
        this.position = position;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get formatted time string (MM:SS. mmm)
     */
    public String getFormattedTime() {
        long minutes = (finishTime / 1000) / 60;
        long seconds = (finishTime / 1000) % 60;
        long millis = finishTime % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }

    /**
     * Get position string with Dutch ordinal (1ste, 2de, 3de, etc.)
     */
    public String getPositionString() {
        return getOrdinal(position);
    }

    /**
     * Get Dutch ordinal for position
     */
    private String getOrdinal(int number) {
        return switch (number) {
            case 1 -> number + "ste";
            case 2 -> number + "de";
            case 3 -> number + "de";
            default -> {
                if (number >= 11 && number <= 13) {
                    yield number + "de";
                }
                yield switch (number % 10) {
                    case 1 -> number + "ste";
                    case 2, 3 -> number + "de";
                    default -> number + "e";
                };
            }
        };
    }

    // Getters
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public long getTime() {
        return finishTime;  // Alias for DataManager compatibility
    }

    public long getDate() {
        return timestamp;  // Alias for DataManager compatibility
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        if (position > 0) {
            return getPositionString() + " - " + playerName + " - " + getFormattedTime();
        }
        return playerName + " - " + getFormattedTime();
    }
}