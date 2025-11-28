package me.Codex.vvKart;

import me.Codex.vvKart.Commands.LeaderboardTest;
import me.Codex.vvKart.Manages.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private TrackManager trackManager;
    private DataManager dataManager;
    private RaceManager raceManager;
    private LeaderBoardManager leaderBoardManager;
    private QueueManager queueManager;

    @Override
    public void onLoad() {

        saveDefaultConfig();

        trackManager = new TrackManager(this);
        dataManager = new DataManager(this);
        dataManager.loadAll();
    }

    @Override
    public void onEnable() {
        raceManager = new RaceManager(this);
        leaderBoardManager = new LeaderBoardManager(this);
        queueManager = new QueueManager(this);
        this.getCommand("testdisplay").setExecutor(new LeaderboardTest());

        leaderBoardManager.loadAllLeaderboards();

        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {

        if (raceManager != null) {
            raceManager.cleanup();
        }
        if (dataManager != null) {
            dataManager.saveAll();
        }
        if (leaderBoardManager != null) {
            leaderBoardManager.removeAllLeaderboards();
        }

        getLogger().info("Plugin disabled!");

    }

    public TrackManager getTrackManager() {
        return trackManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public RaceManager getRaceManager() {
        return raceManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public LeaderBoardManager getLeaderBoardManager() {
        return leaderBoardManager;
    }

    public static Main getInstance() {
        return JavaPlugin.getPlugin(Main.class);
    }
}
