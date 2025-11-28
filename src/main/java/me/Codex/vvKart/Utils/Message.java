package me.Codex.vvKart.Utils;

import me.Codex.vvKart.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Message {

    private static final Main plugin = Main.getInstance();

    public static void send(CommandSender sender, String path) {
        String prefix = plugin.getConfig().getString("messages.prefix");
        String message = plugin.getConfig().getString("messages." + path, path);
        sender.sendMessage(colorize(prefix + message));
    }

    public static void send(CommandSender sender, String path, String placeholder, String value) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&6[VVKart] &r");
        String message = plugin.getConfig(). getString("messages." + path, path);
        message = message.replace("%" + placeholder + "%", value);
        sender.sendMessage(colorize(prefix + message));
    }

    public static void send(CommandSender sender, String path, String...  replacements) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&6[VVKart] &r");
        String message = plugin.getConfig(). getString("messages." + path, path);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("%" + replacements[i] + "%", replacements[i + 1]);
            }
        }

        sender.sendMessage(colorize(prefix + message));
    }

    public static void sendActionBar(Player player, String message) {
        Component component = colorize(message);
        player.sendActionBar(component);
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.showTitle(net.kyori.adventure.title.Title.title(
                colorize(title),
                colorize(subtitle),
                net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(fadeIn * 50L),
                        java.time.Duration.ofMillis(stay * 50L),
                        java.time.Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    public static Component colorize(String text) {
        return LegacyComponentSerializer.legacyAmpersand()
                .deserialize(text)
                .decoration(TextDecoration.ITALIC, false);
    }

    public static String getMessage(String path) {
        return plugin.getConfig().getString("messages." + path, path);
    }

    public static String getMessage(String path, String placeholder, String value) {
        String message = plugin.getConfig().getString("messages." + path, path);
        return message.replace("%" + placeholder + "%", value);
    }
}
