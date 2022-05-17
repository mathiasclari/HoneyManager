package fun.mcbee.bungee.honeynetwork;

import com.google.common.io.ByteStreams;
import fun.mcbee.bungee.honeynetwork.AdminCommands.CheckOnlineCommand;
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
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
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

    public static Map<UUID, FInfo> listFriendsInfos = new HashMap<UUID, FInfo>(); //uuid, info
    public static Map<UUID, FData> listFriendData = new HashMap<UUID, FData>(); //server, info

    public Configuration config;

    public static HoneyNetwork plugin;

    public static HoneyNetwork getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeSettings());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new EventListener(this));
        ProxyServer.getInstance().getPluginManager().registerListener(this, new StaffChatEvent());
//Commands
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new HubCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new CheckOnlineCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new MaintenanceCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BuildServer());
        BungeeCord.getInstance().getPluginManager().registerCommand(this, new FriendCommand("f"));
        BungeeCord.getInstance().getPluginManager().registerCommand(this, new FriendCommand("friend"));
        BungeeCord.getInstance().getPluginManager().registerCommand(this, new FriendsCommand());
        LoadData();
        //maintenance = config.getBoolean("maintenance");
    }

    private void LoadData() {
        GetDefaultConfig();
        String host = config.getString("mysql.host");
        String database = config.getString("mysql.databse");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");
        int port = config.getInt("mysql.port");
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

    /* public void saveDefaultConfig() {
         File file = new File(getDataFolder(), "config.yml");
         try {
             if (file.exists()) {
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
 */
    public static void AddFriendConnection(UUID uuid, String name, boolean update) {
        FData fd = listFriendData.get(uuid);
        if(fd != null) {
            fd.AddConnectionCount();
            if(update) {
                fd.UpdateName(name);
            }
        } else {
            fd = new FData(uuid, name);
            listFriendData.put(uuid, fd);
        }
    }

    public static void RemoveFriendConnections(List<UUID> uuids) {
        for(UUID uuid : uuids) {
            RemoveFriendConnections(uuid);
        }
    }

    public static void RemoveFriendConnections(UUID uuid) {
        listFriendData.get(uuid).RemoveConnectionCount();
    }


}

