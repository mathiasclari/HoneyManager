package fun.mcbee.bungee.honeynetwork.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import fun.mcbee.bungee.honeynetwork.commands.BanCommand;
import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PluginMessaging implements Listener {

    HoneyNetwork plugin;

    public PluginMessaging(HoneyNetwork plugin) {
        this.plugin = plugin;
    }

    public static void sendToBukkit(String channel, String message, ServerInfo server) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(channel);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.sendData("nm:channel", stream.toByteArray());
    }

    @EventHandler
    public void on(PluginMessageEvent e) {
        if(!e.getTag().equalsIgnoreCase("BungeeCord")) {
            return;
        }
        e.setCancelled(true);
        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        String subChannel = in.readUTF();
        if(subChannel.equalsIgnoreCase("ban_information")) {
            String data = in.readUTF();
            String[] splitData = data.split(":");
            ProxiedPlayer player = BungeeCord.getInstance().getPlayer(UUID.fromString(splitData[0]));
            if(player != null) {
                //player.getUniqueId().toString() + ":" + uuid + ":" + GetTime() + ":" + reason + ":" + ip
                UUID uuid = UUID.fromString(splitData[1]);
                Long time = Long.parseLong(splitData[2]);
                String reason = "";
                if(splitData.length > 3) {
                    reason = splitData[3];
                }
                String ip = "";
                if(splitData.length > 4) {
                    ip = splitData[4];
                }
                BanCommand.BanUser(player, uuid, time, reason, ip);
            }
        }
    }

}
