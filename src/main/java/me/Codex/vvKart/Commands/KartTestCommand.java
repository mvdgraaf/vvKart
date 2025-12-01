package me.Codex.vvKart.Commands;

import me.Codex.vvKart.Kart.Kart;
import me.Codex.vvKart.Kart.KartProtocolLib;
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

        // Check player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDit command kan alleen door een speler gebruikt worden!");
            return true;
        }

        // Check of speler al geregistreerd is
        if (KartProtocolLib.inputs.containsKey(player)) {
            player.sendMessage("§cJe zit al in een kart!");
            return true;
        }

        // Registreer speler in input system
        KartProtocolLib.registerPlayer(player);

        // Spawn kart
        kartManager.spawnKart(player);

        player.sendMessage("§aKart gespawned! Gebruik WASD om te rijden.");

        return true;
    }
}
