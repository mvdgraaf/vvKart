package me.Codex.vvKart.Kart;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.Codex.vvKart.Main;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class KartProtocolLib {

    private final Main plugin;
    public static final Map<Player, KartInput> inputs = new HashMap<>();

    public KartProtocolLib(Main plugin) {
        this.plugin = plugin;
    }

    public void register() {

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.STEER_VEHICLE
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();

                if (!inputs.containsKey(player)) return;

                float sideways = event.getPacket().getFloat().read(0);
                float forward = event.getPacket().getFloat().read(1);

                KartInput input = inputs.get(player);
                input.forward = forward;
                input.sideways = sideways;
            }
        });

        plugin.getLogger().info("ProtocolLib kart input listener registered!");
    }

    public static void registerPlayer(Player player) {
        inputs.put(player, new KartInput());
    }

    public static void unregisterPlayer(Player player) {
        if (!inputs.containsKey(player)) return;
        inputs.remove(player);
    }
}
