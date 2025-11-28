package me.Codex.vvKart.Utils;

public class Time {

    public static String formatTime(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        long millis = milliseconds % 1000;

        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }

    public static String formatSeconds(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;

        return String.format("%02d:%02d", minutes, secs);
    }

    public static long now() {
        return System.currentTimeMillis();
    }
}
