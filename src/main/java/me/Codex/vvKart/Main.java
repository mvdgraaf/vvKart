package me.Codex.vvKart;


import me.Codex.vvKart.Commands.LeaderboardTest;
import me.Codex.vvKart.Commands.VVKartCommand;
import me.Codex.vvKart.Kart.KartController;
import me.Codex.vvKart.Listeners.PlayerLeaveEvent;
import me.Codex.vvKart.Listeners.VehicleExitListener;
import me.Codex.vvKart.Manages.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {

    private TrackManager trackManager;
    private DataManager dataManager;
    private RaceManager raceManager;
    private LeaderBoardManager leaderBoardManager;
    private QueueManager queueManager;
    private KartController kartController;
    //
//    @Override
//    public void onLoad() {
//
//    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        trackManager = new TrackManager(this);
        dataManager = new DataManager(this);
        raceManager = new RaceManager(this);
        leaderBoardManager = new LeaderBoardManager(this);
        queueManager = new QueueManager(this);

        registerListeners();

        kartController = new KartController(this);
        kartController.register();

        Objects.requireNonNull(this.getCommand("testdisplay")).setExecutor(new LeaderboardTest());
        Objects.requireNonNull(getCommand("vvkart")).setExecutor(new VVKartCommand(this));

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


    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new VehicleExitListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(Main.getInstance()), this);
        getServer().getPluginManager().registerEvents(new VehicleExitListener(), this);

        // World load listener
        getServer().getPluginManager().registerEvents(new org.bukkit.event. Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
            public void onWorldLoad(org.bukkit.event.world.WorldLoadEvent event) {
                // Als eerste wereld geladen is, laad dan alle tracks
                if (event. getWorld().getName().equals("world")) {
                    Bukkit.getScheduler().runTask(Main.this, () -> {
                        dataManager.loadAll();
                        leaderBoardManager. loadAllLeaderboards();
                        getLogger().info("Tracks and leaderboards loaded after world initialization!");
                    });
                }
            }
        }, this);
    }

    public static Main getInstance() {
        return JavaPlugin.getPlugin(Main.class);
    }
}
