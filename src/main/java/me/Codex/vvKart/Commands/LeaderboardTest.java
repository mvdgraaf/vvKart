package me.Codex.vvKart.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;

public class LeaderboardTest implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        Player player = (Player) sender;
        Location location = ((Player) sender).getLocation();
        location.add(0, 2, 0);
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());

        World world = location.getWorld();
        String track = "Test track";

        String title = " Leaderboard " + track;

        StringBuilder line = new StringBuilder();
        for (int i = 0; i < title.length(); i++) {
            line.append("▬");
        }
        TextDisplay display = world.spawn(location, TextDisplay.class, entity -> {

            Component text = Component.text()
                    .append(Component.text(title + "\n", TextColor.fromHexString("#00aaaa")))
                    .append(Component.text(line.toString() + "\n\n", NamedTextColor.WHITE))  // dynamische lengte
                    .append(Component.text("1. Henk\n", NamedTextColor.WHITE))
                    .append(Component.text("2. Jan\n", NamedTextColor.WHITE))
                    .append(Component.text("3. Piet\n", NamedTextColor.WHITE))
                    .append(Component.text("4. Peter\n", NamedTextColor.WHITE))
                    .append(Component.text("5. Lullo\n", NamedTextColor.WHITE))
                    .append(Component.text("6. Janus\n", NamedTextColor.WHITE))
                    .append(Component.text("7. Hakkuh\n", NamedTextColor.WHITE))
                    .append(Component.text("8. Boren\n", NamedTextColor.WHITE))
                    .append(Component.text("9. Gamer\n", NamedTextColor.WHITE))
                    .append(Component.text("10. Grapjas", NamedTextColor.WHITE))
                    .build();
            entity.setSeeThrough(true);
            entity.text(text);
        });

        return true;
    }
}
