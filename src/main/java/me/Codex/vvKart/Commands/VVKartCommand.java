package me.Codex.vvKart.Commands;

import me.Codex. vvKart.Main;
import me.Codex.vvKart.Models.Checkpoint;
import me.Codex.vvKart.Models.Track;
import me.Codex.vvKart.Utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit. command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VVKartCommand implements CommandExecutor {

    private final Main plugin;

    private final Map<UUID, Map<Integer, Location>> tempCheckpointPositions;
    private final Map<UUID, Location> tempFinishPos1;

    public VVKartCommand(Main plugin) {
        this.plugin = plugin;
        this.tempCheckpointPositions = new HashMap<>();
        this.tempFinishPos1 = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            // Track management
            case "create" -> handleCreate(sender, args);
            case "delete", "verwijder" -> handleDelete(sender, args);
            case "list", "lijst" -> handleList(sender);
            case "info" -> handleInfo(sender, args);

            // Setup commands
            case "sethub" -> handleSetHub(sender, args);
            case "setstar/vtplek", "setstart" -> handleSetStartPosition(sender, args);
            case "setfinish" -> handleSetFinish(sender, args);
            case "delfinish" -> handleDelFinish(sender, args);

            // Checkpoint commands
            case "createcheckpoint", "addcheckpoint" -> handleCreateCheckpoint(sender, args);
            case "delcheckpoint", "removecheckpoint" -> handleDelCheckpoint(sender, args);
            case "listcheckpoints" -> handleListCheckpoints(sender, args);

            // Track status
            case "open", "openen" -> handleOpen(sender, args);
            case "close", "dicht", "sluiten" -> handleClose(sender, args);
            case "setlaps", "rondes" -> handleSetLaps(sender, args);

            // Leaderboard commands
            case "setleaderboard", "leaderboard" -> handleSetLeaderboard(sender, args);
            case "delleaderboard" -> handleDelLeaderboard(sender, args);

            // Player commands
            case "join" -> handleJoin(sender, args);
            case "leave", "verlaat" -> handleLeave(sender);

            // Admin commands
            case "reload" -> handleReload(sender);
            case "forcestart" -> handleForceStart(sender, args);
            case "forcestop" -> handleForceStop(sender, args);

            default -> sendHelp(sender);
        }

        return true;
    }

    // ============================================
    // TRACK MANAGEMENT COMMANDS
    // ============================================

    private void handleCreate(CommandSender sender, String[] args) {
        if (! sender.hasPermission("vvkart.create")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cGebruik: /vvkart create <naam>");
            return;
        }

        String trackName = args[1];

        if (plugin.getTrackManager(). createTrack(trackName)) {
            Message.send(sender, "track-created", "track", trackName);
        } else {
            Message.send(sender, "track-already-exists", "track", trackName);
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vvkart.delete")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cGebruik: /vvkart delete <naam>");
            return;
        }

        String trackName = args[1];

        if (plugin.getTrackManager().deleteTrack(trackName)) {
            Message. send(sender, "track-deleted", "track", trackName);
        } else {
            Message. send(sender, "track-not-found", "track", trackName);
        }
    }

    private void handleList(CommandSender sender) {
        if (! sender.hasPermission("vvkart.list")) {
            Message.send(sender, "no-permission");
            return;
        }

        var tracks = plugin.getTrackManager().getAllTracks();

        if (tracks.isEmpty()) {
            sender.sendMessage("§cEr zijn geen banen aangemaakt!");
            return;
        }

        sender.sendMessage("§6§l▬▬▬▬▬▬▬ VVKart Banen ▬▬▬▬▬▬▬");
        for (Track track : tracks) {
            String status = track.isOpen() ? "§a✓ Open" : "§c✗ Gesloten";
            boolean ready = plugin.getTrackManager(). isTrackReady(track);
            String readyStatus = ready ? "§a[Klaar]" : "§e[Setup]";

            sender.sendMessage("§e" + track.getName() + " " + readyStatus + " " + status);
        }
        sender.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (! sender.hasPermission("vvkart.info")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args. length < 2) {
            sender.sendMessage("§cGebruik: /vvkart info <naam>");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[1]);
            return;
        }

        sender.sendMessage("§6§l▬▬▬▬▬ Track Info: " + track.getName() + " ▬▬▬▬▬");
        sender.sendMessage("§eStatus: " + (track.isOpen() ? "§aOpen" : "§cGesloten"));
        sender.sendMessage("§eHub: " + (track.getHub() != null ? "§a✓" : "§c✗"));
        sender.sendMessage("§eFinish: " + (track.getFinishPos2() != null ? "§a✓" : "§c✗"));
        sender.sendMessage("§eStartposities: §f" + track.getStartPositions().size());
        sender.sendMessage("§eCheckpoints: §f" + track. getCheckpoints().size());
        sender.sendMessage("§eRondes: §f" + track.getLaps());
        sender.sendMessage("§eMin.  spelers: §f" + track. getMinPlayers());

        List<String> errors = plugin.getTrackManager().getTrackErrors(track);
        if (!errors.isEmpty()) {
            sender.sendMessage("§c§lProblemen:");
            for (String error : errors) {
                sender.sendMessage("  §c- " + error);
            }
        } else {
            sender.sendMessage("§a§l✓ Baan is klaar om te gebruiken!");
        }

        sender.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    // ============================================
    // SETUP COMMANDS
    // ============================================

    private void handleSetHub(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cAlleen spelers kunnen dit command gebruiken!");
            return;
        }

        if (!sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cGebruik: /vvkart sethub <naam>");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[1]);
            return;
        }

        track.setHub(player.getLocation());
        plugin.getDataManager().saveTrack(track);
        Message.send(sender, "hub-set", "track", track.getName());
    }

    private void handleSetStartPosition(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cAlleen spelers kunnen dit command gebruiken!");
            return;
        }

        if (! sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args. length < 3) {
            sender.sendMessage("§cGebruik: /vvkart setstartplek <positie> <naam>");
            return;
        }

        int position;
        try {
            position = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cPositie moet een nummer zijn!");
            return;
        }

        if (position < 1 || position > 10) {
            sender.sendMessage("§cPositie moet tussen 1 en 10 zijn!");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[2]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[2]);
            return;
        }

        track.setStartPosition(position, player.getLocation());
        plugin.getDataManager().saveTrack(track);
        Message.send(sender, "start-position-set", "position", String.valueOf(position), "track", track.getName());
    }

    private void handleSetFinish(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cAlleen spelers kunnen dit command gebruiken!");
            return;
        }

        if (! sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cGebruik: /vvkart setfinish <pos1/pos2> <naam>");
            return;
        }

        String posType = args[1].toLowerCase();
        Track track = plugin.getTrackManager().getTrack(args[2]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[2]);
            return;
        }

        Location location = player.getLocation();
        UUID playerUUID = player.getUniqueId();

        if (posType.equals("pos1")) {
            tempFinishPos1.put(playerUUID, location);
            player.sendMessage("§a§l✓ Finish positie 1 ingesteld!");
            player.sendMessage("§eGa naar de andere kant van de finish lijn:");
            player.sendMessage("§f/vvkart setfinish pos2 " + track.getName());

        } else if (posType.equals("pos2")) {
            Location pos1 = tempFinishPos1. get(playerUUID);

            if (pos1 == null) {
                sender.sendMessage("§cJe hebt nog geen pos1 ingesteld!");
                sender.sendMessage("§eGebruik eerst: §f/vvkart setfinish pos1 " + track.getName());
                return;
            }

            Location pos2 = location;

            if (! pos1.getWorld().equals(pos2.getWorld())) {
                sender.sendMessage("§cPos1 en pos2 moeten in dezelfde wereld zijn!");
                tempFinishPos1.remove(playerUUID);
                return;
            }

            double distance = pos1.distance(pos2);
            if (distance < 1.0) {
                sender.sendMessage("§cPos1 en pos2 zijn te dichtbij elkaar!");
                return;
            }

            track.setFinishZone(pos1, pos2);
            plugin.getDataManager().saveTrack(track);
            tempFinishPos1.remove(playerUUID);

            Message.send(sender, "finish-set", "track", track.getName());
            sender.sendMessage("§aFinish zone breedte: §e" + String.format("%.2f", distance) + " blokken");

        } else {
            sender.sendMessage("§cGebruik: pos1 of pos2!");
        }
    }

    private void handleDelFinish(CommandSender sender, String[] args) {
        if (! sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args. length < 2) {
            sender.sendMessage("§cGebruik: /vvkart delfinish <naam>");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[1]);
            return;
        }

        track.setFinishZone(null, null);
        plugin.getDataManager().saveTrack(track);
        Message.send(sender, "finish-deleted", "track", track.getName());
    }

    // ============================================
    // CHECKPOINT COMMANDS
    // ============================================

    private void handleCreateCheckpoint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cAlleen spelers kunnen dit command gebruiken!");
            return;
        }

        if (!sender.hasPermission("vvkart. setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 4) {
            sender.sendMessage("§cGebruik: /vvkart createcheckpoint <pos1/pos2> <nummer> <naam>");
            return;
        }

        String posType = args[1].toLowerCase();
        int number;

        try {
            number = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cNummer moet een getal zijn!");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[3]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[3]);
            return;
        }

        // Store position temporarily
        Location location = player.getLocation();
        UUID playerUUID = player.getUniqueId();

        if (posType.equals("pos1")) {
            // Store pos1 in temporary storage
            tempCheckpointPositions.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(number, location);

            player.sendMessage("§a§l✓ Positie 1 ingesteld voor checkpoint " + number + "!");
            player.sendMessage("§eGa naar de tweede hoek en gebruik:");
            player.sendMessage("§f/vvkart createcheckpoint pos2 " + number + " " + track. getName());

        } else if (posType. equals("pos2")) {
            // Get pos1 from temporary storage
            Map<Integer, Location> playerCheckpoints = tempCheckpointPositions. get(playerUUID);

            if (playerCheckpoints == null || ! playerCheckpoints.containsKey(number)) {
                sender.sendMessage("§cJe hebt nog geen pos1 ingesteld voor checkpoint " + number + "!");
                sender.sendMessage("§eGebruik eerst: §f/vvkart createcheckpoint pos1 " + number + " " + track.getName());
                return;
            }

            Location pos1 = playerCheckpoints. get(number);
            Location pos2 = location;

            // Verify they're in the same world
            if (!pos1.getWorld().equals(pos2.getWorld())) {
                sender. sendMessage("§cPos1 en pos2 moeten in dezelfde wereld zijn!");
                tempCheckpointPositions.get(playerUUID).remove(number);
                return;
            }

            // Create checkpoint
            Checkpoint checkpoint = new Checkpoint(number, pos1, pos2);
            track.addCheckpoint(checkpoint);
            plugin.getDataManager().saveTrack(track);

            // Remove from temporary storage
            tempCheckpointPositions.get(playerUUID).remove(number);

            Message.send(sender, "checkpoint-created", "number", String.valueOf(number), "track", track.getName());
            sender.sendMessage("§aCheckpoint volume: §e" + String.format("%.2f", checkpoint.getVolume()) + " blokken³");

        } else {
            sender. sendMessage("§cGebruik: pos1 of pos2!");
            sender.sendMessage("§eVoorbeeld: §f/vvkart createcheckpoint pos1 1 " + track.getName());
        }
    }

    private void handleDelCheckpoint(CommandSender sender, String[] args) {
        if (! sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args. length < 3) {
            sender.sendMessage("§cGebruik: /vvkart delcheckpoint <nummer> <naam>");
            return;
        }

        int number;
        try {
            number = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender. sendMessage("§cNummer moet een getal zijn!");
            return;
        }

        Track track = plugin.getTrackManager(). getTrack(args[2]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[2]);
            return;
        }

        if (track.removeCheckpoint(number)) {
            plugin.getDataManager().saveTrack(track);
            Message. send(sender, "checkpoint-deleted", "number", String.valueOf(number), "track", track.getName());
        } else {
            sender.sendMessage("§cCheckpoint niet gevonden!");
        }
    }

    private void handleListCheckpoints(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cGebruik: /vvkart listcheckpoints <naam>");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[1]);
            return;
        }

        List<Checkpoint> checkpoints = track.getCheckpoints();

        if (checkpoints.isEmpty()) {
            sender. sendMessage("§cGeen checkpoints gevonden voor deze baan!");
            return;
        }

        sender.sendMessage("§6§l▬▬▬ Checkpoints: " + track.getName() + " ▬▬▬");
        for (Checkpoint cp : checkpoints) {
            sender.sendMessage("§eCheckpoint " + cp.getNumber() + ": §f" + cp);
        }
        sender. sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    // ============================================
    // TRACK STATUS COMMANDS
    // ============================================

    private void handleOpen(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vvkart. setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cGebruik: /vvkart open <naam>");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message. send(sender, "track-not-found", "track", args[1]);
            return;
        }

        if (! plugin.getTrackManager().isTrackReady(track)) {
            sender.sendMessage("§cDeze baan is nog niet klaar!  Gebruik /vvkart info " + track.getName() + " voor details.");
            return;
        }

        track.setOpen(true);
        plugin.getDataManager().saveTrack(track);
        Message.send(sender, "track-opened", "track", track.getName());
    }

    private void handleClose(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cGebruik: /vvkart close <naam>");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[1]);
            return;
        }

        track.setOpen(false);
        plugin.getDataManager().saveTrack(track);
        Message. send(sender, "track-closed", "track", track.getName());
    }

    private void handleSetLaps(CommandSender sender, String[] args) {
        if (! sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args. length < 3) {
            sender.sendMessage("§cGebruik: /vvkart setlaps <aantal> <naam>");
            return;
        }

        int laps;
        try {
            laps = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAantal moet een getal zijn!");
            return;
        }

        if (laps < 1 || laps > 100) {
            sender.sendMessage("§cAantal rondes moet tussen 1 en 100 zijn!");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[2]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[2]);
            return;
        }

        track.setLaps(laps);
        plugin.getDataManager().saveTrack(track);
        sender.sendMessage("§aAantal rondes ingesteld op §e" + laps + " §avoor baan §e" + track.getName() + "§a!");
    }

    // ============================================
    // LEADERBOARD COMMANDS
    // ============================================

    private void handleSetLeaderboard(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cAlleen spelers kunnen dit command gebruiken!");
            return;
        }

        if (! sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args. length < 2) {
            sender.sendMessage("§cGebruik: /vvkart setleaderboard <naam>");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[1]);
            return;
        }

        if (plugin.getLeaderBoardManager().createLeaderboard(track, player. getLocation())) {
            Message.send(sender, "leaderboard-set", "track", track.getName());
        } else {
            sender.sendMessage("§cKon leaderboard niet aanmaken!");
        }
    }

    private void handleDelLeaderboard(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vvkart.setup")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cGebruik: /vvkart delleaderboard <naam>");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[1]);
            return;
        }

        if (plugin. getLeaderBoardManager().removeLeaderboard(track)) {
            Message.send(sender, "leaderboard-deleted", "track", track.getName());
        } else {
            sender.sendMessage("§cGeen leaderboard gevonden!");
        }
    }

    // ============================================
    // PLAYER COMMANDS
    // ============================================

    private void handleJoin(CommandSender sender, String[] args) {
        Player targetPlayer;
        String trackName;

        // Check if console with player argument
        if (!(sender instanceof Player)) {
            if (args.length < 3) {
                sender.sendMessage("§cGebruik (console): /vvkart join <speler> <baan>");
                return;
            }

            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage("§cSpeler niet online!");
                return;
            }

            trackName = args[2];
        } else {
            if (args.length < 2) {
                sender.sendMessage("§cGebruik: /vvkart join <baan>");
                return;
            }

            targetPlayer = (Player) sender;
            trackName = args[1];

            if (!sender.hasPermission("vvkart.use")) {
                Message.send(sender, "no-permission");
                return;
            }
        }

        Track track = plugin.getTrackManager().getTrack(trackName);

        if (track == null) {
            sender.sendMessage("§cBaan niet gevonden!");
            return;
        }

        plugin.getQueueManager().addToQueue(targetPlayer, track);

        if (! sender.equals(targetPlayer)) {
            sender.sendMessage("§aSpeler §e" + targetPlayer.getName() + " §ais toegevoegd aan §e" + track.getName() + "§a!");
        }
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cAlleen spelers kunnen dit command gebruiken!");
            return;
        }

        if (!sender.hasPermission("vvkart.leave")) {
            Message.send(sender, "no-permission");
            return;
        }

        plugin.getRaceManager().removeFromRace(player);
    }

    // ============================================
    // ADMIN COMMANDS
    // ============================================

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("vvkart. reload")) {
            Message.send(sender, "no-permission");
            return;
        }

        plugin.reloadConfig();
        plugin.getDataManager().loadAll();
        Message.send(sender, "reload-success");
    }

    private void handleForceStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vvkart.admin")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cGebruik: /vvkart forcestart <baan>");
            return;
        }

        Track track = plugin. getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[1]);
            return;
        }

        List<Player> players = plugin.getQueueManager().getQueuePlayers(track);

        if (players.isEmpty()) {
            sender.sendMessage("§cGeen spelers in de wachtrij!");
            return;
        }

        plugin.getRaceManager().startRace(track, players);
        sender.sendMessage("§aRace geforceerd gestart!");
    }

    private void handleForceStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vvkart.admin")) {
            Message.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cGebruik: /vvkart forcestop <baan>");
            return;
        }

        Track track = plugin.getTrackManager().getTrack(args[1]);

        if (track == null) {
            Message.send(sender, "track-not-found", "track", args[1]);
            return;
        }

        plugin.getRaceManager().cancelRace(track);
        sender.sendMessage("§aRace gestopt!");
    }

    // ============================================
    // HELP MENU
    // ============================================

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l▬▬▬▬▬▬▬▬ VVKart Commands ▬▬▬▬▬▬▬▬");
        sender.sendMessage("§e§lTrack Management:");
        sender.sendMessage("  §e/vvkart create <naam> §7- Maak een nieuwe baan");
        sender.sendMessage("  §e/vvkart delete <naam> §7- Verwijder een baan");
        sender.sendMessage("  §e/vvkart list §7- Lijst van alle banen");
        sender. sendMessage("  §e/vvkart info <naam> §7- Baan informatie");
        sender.sendMessage("");
        sender.sendMessage("§e§lSetup:");
        sender.sendMessage("  §e/vvkart sethub <naam> §7- Zet hub locatie");
        sender.sendMessage("  §e/vvkart setstartplek <1-10> <naam> §7- Zet startpositie");
        sender.sendMessage("  §e/vvkart setfinish <naam> §7- Zet finish lijn");
        sender.sendMessage("  §e/vvkart createcheckpoint <pos1/pos2> <nr> <naam>");
        sender.sendMessage("  §e/vvkart setlaps <aantal> <naam> §7- Zet aantal rondes");
        sender.sendMessage("");
        sender.sendMessage("§e§lBaan Status:");
        sender.sendMessage("  §e/vvkart open <naam> §7- Open baan");
        sender.sendMessage("  §e/vvkart close <naam> §7- Sluit baan");
        sender.sendMessage("");
        sender.sendMessage("§e§lSpeler:");
        sender.sendMessage("  §e/vvkart join <baan> §7- Join een race");
        sender.sendMessage("  §e/vvkart leave §7- Verlaat race");
        sender.sendMessage("");
        sender.sendMessage("§e§lAdmin:");
        sender.sendMessage("  §e/vvkart reload §7- Herlaad plugin");
        sender.sendMessage("  §e/vvkart setleaderboard <naam> §7- Plaats leaderboard");
        sender. sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
}
