package fun.mcbee.bungee.honeynetwork;

import com.google.common.io.ByteStreams;
import fun.mcbee.bungee.honeynetwork.commands.*;
import fun.mcbee.bungee.honeynetwork.listener.PluginMessaging;
import fun.mcbee.bungee.honeynetwork.Util.EventListener;
import fun.mcbee.bungee.honeynetwork.data.FData;
import fun.mcbee.bungee.honeynetwork.data.FInfo;
import fun.mcbee.bungee.honeynetwork.data.MYSQL;
import fun.mcbee.bungee.honeynetwork.data.PData;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class HoneyNetwork extends Plugin {

    public static Map<UUID, FInfo> listFriendsInfos = new HashMap<>();

    public static Map<UUID, FData> listFriendData = new HashMap<>();

    public static Map<UUID, PData> listPlayerData = new HashMap<>();
    public static ChatColor successColor;
    public static ChatColor errorColor;
    public static Map<UUID, Long> bannedPlayers = new HashMap<UUID, Long>();

    public Configuration config;

    public static HoneyNetwork plugin;

    public static HoneyNetwork getInstance() {
        return plugin;
    }

    public void onEnable() {
        plugin = this;
        ProxyServer.getInstance().getPluginManager().registerListener(this, new EventListener(this));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, (Command)new HubCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, (Command)new CheckOnlineCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, (Command)new BuildServer());
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command)new FriendCommand("f"));
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command)new FriendCommand("friend"));
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command)new goCommand());
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command)new FriendsCommand());
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command) new PlayTime());
        BungeeCord.getInstance().getPluginManager().registerCommand(this, (Command) new BanCommand());
        BungeeCord.getInstance().registerChannel("nm:channel");
        BungeeCord.getInstance().pluginManager.registerListener(this, new PluginMessaging(this));
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

        BungeeCord.getInstance().getScheduler().runAsync(this, new Runnable() {
            @Override
            public void run() {
                Statement stmt = null;
                String query = "SELECT * FROM NetworkManagerBans WHERE time_ban_expires != 0;";
                try {
                    stmt = MYSQL.GetConnection().createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        System.out.println(uuid.toString());
                        long time = rs.getLong("time_ban_expires");
                        bannedPlayers.put(uuid, time);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if(stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
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