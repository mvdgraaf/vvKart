package me.Codex.vvKart.Manages;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.Track;

public class LeaderBoardManager {

    private final Main plugin;
    private static final int SIGNS_PER_LEADERBOARD = 10;

    public LeaderBoardManager(Main plugin) {
        this.plugin = plugin;
    }

    public void updateLeaderBoard(Track track) {
        if (track.getLeaderboard() != null) {

        }
    }
}
