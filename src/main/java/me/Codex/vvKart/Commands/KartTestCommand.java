package me.Codex.vvKart.Commands;

import me.Codex.vvKart.Kart.Kart;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KartTestCommand implements CommandExecutor {

    private final Kart kartManager;

    public KartTestCommand(Kart kartManager) {
        this.kartManager = kartManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDit command kan alleen door een speler gebruikt worden!");
            return true;
        }

        // Subcommand voor verwijderen
        if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
            if (Kart.getPig(player) == null) {
                player.sendMessage("§cJe hebt geen kart actief.");
                return true;
            }
            Kart.despawnKart(player);
            player.sendMessage("§eKart verwijderd.");
            return true;
        }

        // Subcommand voor speed aanpassen
        if (args.length > 0 && args[0].equalsIgnoreCase("speed")) {
            Kart.KartStats stats = Kart.getStats(player);
            if (stats == null) {
                player.sendMessage("§cJe hebt geen kart actief.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("§eHuidige snelheid: §f" + String.format("%.2f", stats.maxSpeed));
                player.sendMessage("§eGebruik: /" + label + " speed <waarde>");
                return true;
            }

            try {
                double speed = Double.parseDouble(args[1]);
                if (speed <= 0 || speed > 5.0) {
                    player.sendMessage("§cSnelheid moet tussen 0 en 5.0 zijn!");
                    return true;
                }
                stats.maxSpeed = speed;
                player.sendMessage("§aMax snelheid ingesteld op: §f" + speed);
            } catch (NumberFormatException e) {
                player.sendMessage("§cOngeldig getal!");
            }
            return true;
        }

        // Subcommand voor multiplier aanpassen
        if (args.length > 0 && args[0].equalsIgnoreCase("multiplier")) {
            Kart.KartStats stats = Kart.getStats(player);
            if (stats == null) {
                player.sendMessage("§cJe hebt geen kart actief.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("§eHuidige multiplier: §f" + String.format("%.2f", stats.speedMultiplier));
                player.sendMessage("§eGebruik: /" + label + " multiplier <waarde>");
                return true;
            }

            try {
                double multiplier = Double.parseDouble(args[1]);
                if (multiplier <= 0 || multiplier > 10.0) {
                    player.sendMessage("§cMultiplier moet tussen 0 en 10.0 zijn!");
                    return true;
                }
                stats.speedMultiplier = multiplier;
                player.sendMessage("§aSpeed multiplier ingesteld op: §f" + multiplier);
            } catch (NumberFormatException e) {
                player.sendMessage("§cOngeldig getal!");
            }
            return true;
        }

        // Subcommand voor info
        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            Kart.KartStats stats = Kart.getStats(player);
            if (stats == null) {
                player.sendMessage("§cJe hebt geen kart actief.");
                return true;
            }

            player.sendMessage("§e§l=== Kart Info ===");
            player.sendMessage("§eMax Snelheid: §f" + String.format("%.2f", stats.maxSpeed));
            player.sendMessage("§eSpeed Multiplier: §f" + String.format("%.2f", stats.speedMultiplier));
            player.sendMessage("§eHuidige Snelheid: §f" + String.format("%.2f", stats.currentSpeed));
            player.sendMessage("§eDrift Factor: §f" + String.format("%.2f", stats.driftFactor));
            player.sendMessage("§eAan het driften: §f" + (stats.isDrifting ? "Ja" : "Nee"));
            return true;
        }

        // Spawn nieuw kart als speler er nog niet in zit
        if (Kart.getPig(player) != null) {
            player.sendMessage("§cJe zit al in een kart! Gebruik /" + label + " remove om hem te verwijderen.");
            return true;
        }

        kartManager.spawnKart(player);
        player.sendMessage("§aKart gespawned! Gebruik WASD om te rijden.");
        player.sendMessage("§7/" + label + " remove §8- §7Verwijder kart");
        player.sendMessage("§7/" + label + " speed <waarde> §8- §7Pas max snelheid aan");
        player.sendMessage("§7/" + label + " multiplier <waarde> §8- §7Pas snelheid multiplier aan");
        player.sendMessage("§7/" + label + " info §8- §7Bekijk kart statistieken");
        return true;
    }
}

