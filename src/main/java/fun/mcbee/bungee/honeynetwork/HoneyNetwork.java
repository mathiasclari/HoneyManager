package fun.mcbee.bungee.honeynetwork;

import com.google.common.io.ByteStreams;
import fun.mcbee.bungee.honeynetwork.AdminCommands.CheckOnlineCommand;
import fun.mcbee.bungee.honeynetwork.AdminCommands.PlayTime;
import fun.mcbee.bungee.honeynetwork.FriendsCommands.FriendCommand;
import fun.mcbee.bungee.honeynetwork.FriendsCommands.FriendsCommand;
import fun.mcbee.bungee.honeynetwork.ManagerCommands.MaintenanceCommand;
import fun.mcbee.bungee.honeynetwork.ProxySettings.BungeeSettings;
import fun.mcbee.bungee.honeynetwork.ServerCommands.BuildServer;
import fun.mcbee.bungee.honeynetwork.ServerCommands.HubCommand;
import fun.mcbee.bungee.honeynetwork.StaffChat.StaffChatEvent;
import fun.mcbee.bungee.honeynetwork.Util.EventListener;
import fun.mcbee.bungee.honeynetwork.data.FData;
import fun.mcbee.bungee.honeynetwork.data.FInfo;
import fun.mcbee.bungee.honeynetwork.data.MYSQL;
import fun.mcbee.bungee.honeynetwork.data.PData;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class HoneyNetwork extends Plugin {

    public static boolean maintenance = false;

    public static Map<UUID, FInfo> listFriendsInfos = new HashMap<>();

    public static Map<UUID, FData> listFriendData = new HashMap<>();

    public static Map<UUID, PData> listPlayerData = new HashMap<>();

    public Configuration config;

    public static HoneyNetwork plugin;

    public static HoneyNetwork getInstance() {
        return plugin;
    }

    public void onEnable() {
        plugin = this;
        ProxyServer.getInstance().getPluginManager().registerListener(this, (Listener)new BungeeSettings());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new EventListener(this));
        ProxyServer.getInstance().getPluginManager().registerListener(this, (Listener)new StaffChatEvent());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, (Command)new HubCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, (Command)new CheckOnlineCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, (Command)new MaintenanceCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, (Command)new BuildServer());
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command)new FriendCommand("f"));
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command)new FriendCommand("friend"));
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command)new FriendsCommand());
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command)new PlayTime());
        LoadData();
    }

    private void LoadData() {
        GetDefaultConfig();
        String host = this.config.getString("mysql.host");
        String database = this.config.getString("mysql.databse");
        String username = this.config.getString("mysql.username");
        String password = this.config.getString("mysql.password");
        int port = this.config.getInt("mysql.port");
        MYSQL.Login(host, database, username, password, port);
        MYSQL.CreateTables();
    }

    void GetDefaultConfig() {
        File file = new File(getDataFolder(), "config.yml");
        try {
            if (!file.exists()) {
                if (!getDataFolder().exists())
                    getDataFolder().mkdir();
                file.createNewFile();
                InputStream is = getResourceAsStream("config.yml");
                Throwable localThrowable6 = null;
                try {
                    OutputStream os = new FileOutputStream(file);
                    try {
                        ByteStreams.copy(is, os);
                    } catch (Throwable localThrowable1) {
                        throw localThrowable1;
                    }
                } catch (Throwable localThrowable4) {
                    localThrowable6 = localThrowable4;
                    throw localThrowable4;
                } finally {
                    if (is != null)
                        if (localThrowable6 != null) {
                            try {
                                is.close();
                            } catch (Throwable localThrowable5) {
                                localThrowable6.addSuppressed(localThrowable5);
                            }
                        } else {
                            is.close();
                        }
                }
            }
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void AddFriendConnection(UUID uuid, String name, boolean update) {
        FData fd = listFriendData.get(uuid);
        if (fd != null) {
            fd.AddConnectionCount();
            if (update)
                fd.UpdateName(name);
        } else {
            fd = new FData(uuid, name);
            listFriendData.put(uuid, fd);
        }
    }

    public static void RemoveFriendConnections(List<UUID> uuids) {
        for (UUID uuid : uuids)
            RemoveFriendConnections(uuid);
    }

    public static void RemoveFriendConnections(UUID uuid) {
        ((FData)listFriendData.get(uuid)).RemoveConnectionCount();
    }
}