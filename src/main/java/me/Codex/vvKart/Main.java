package me.Codex.vvKart;

import me.Codex.vvKart.Manages.DataManager;
import me.Codex.vvKart.Manages.LeaderBoardManager;
import me.Codex.vvKart.Manages.RaceManager;
import me.Codex.vvKart.Manages.TrackManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private TrackManager trackManager;
    private DataManager dataManager;
    private RaceManager raceManager;
    private LeaderBoardManager leaderBoardManager;

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
        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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

    public LeaderBoardManager getLeaderBoardManager() {
        return leaderBoardManager;
    }

    public static Main getInstance() {;
        return JavaPlugin.getPlugin(Main.class);
    }
}
