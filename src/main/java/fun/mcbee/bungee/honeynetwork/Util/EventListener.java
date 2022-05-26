package fun.mcbee.bungee.honeynetwork.Util;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import fun.mcbee.bungee.honeynetwork.commands.BanCommand;
import fun.mcbee.bungee.honeynetwork.data.FInfo;
import fun.mcbee.bungee.honeynetwork.data.MYSQL;
import fun.mcbee.bungee.honeynetwork.data.PData;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EventListener implements Listener {
    HoneyNetwork plugin;

    public EventListener(HoneyNetwork plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void OnServerConnectEvent(ServerConnectEvent e) {
        if (e.getPlayer().getServer() == null) {
            UUID uuid = e.getPlayer().getUniqueId();

            Long timeBanExpires = HoneyNetwork.bannedPlayers.get(uuid);
            if(timeBanExpires != null) {
                if(timeBanExpires != -1 && System.currentTimeMillis() - timeBanExpires >= 0) {
                    HoneyNetwork.bannedPlayers.remove(uuid);
                    BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            Statement stmt = null;
                            String query = "UPDATE NetworkManagerBans SET time_ban_expires = '0' WHERE (uuid = '" + uuid + "');";
                            try {
                                stmt = MYSQL.GetConnection().createStatement();
                                stmt.executeUpdate(query);
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
                } else {
                    TextComponent kickTitle = new TextComponent("You have been banned from the server!\n");
                    kickTitle.setColor(HoneyNetwork.errorColor);
                    kickTitle.setBold(true);

                    TextComponent message;

                    if(timeBanExpires <= -1) {
                        message = new TextComponent("You are permanently banned from the McBee!");
                    } else {
                        String time = BanCommand.GetTextTime((timeBanExpires - System.currentTimeMillis()) / 1000);
                        message = new TextComponent("You are banned for " + time + " from the McBee!");
                    }
                    message.setColor(ChatColor.of("#FFBF00"));

                    TextComponent finalMessage = new TextComponent();
                    finalMessage.addExtra(kickTitle);
                    finalMessage.addExtra(message);

                    e.getPlayer().disconnect(finalMessage);
                    e.setCancelled(true);
                    return;
                }
            }

            PData pd = new PData(uuid);
            HoneyNetwork.listPlayerData.put(uuid, pd);
            FInfo fi = new FInfo(uuid, e.getPlayer().getName());
            HoneyNetwork.listFriendsInfos.put(uuid, fi);
            BungeeCord.getInstance().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    fi.SendLogMessage(e.getPlayer(), ChatColor.BLUE + e.getPlayer().getName() + " joined!");
                }
            }, 5, TimeUnit.SECONDS);
            //create profile or update name of the user
            //load all friends uuids and names (mabye nicknames)
            //by loading friends add a number to them

        }
    }

    @EventHandler
    public void OnPlayerDataDumpedEvent(PlayerDisconnectEvent e) {
        //System.out.println("PlayerDataDumpedEvent: " + e.getPlayerData().GetUUID());
        UUID uuid = e.getPlayer().getUniqueId();
        PData pd = HoneyNetwork.listPlayerData.remove(uuid);
        if(pd != null) {
            pd.Logout();
            HoneyNetwork.listFriendsInfos.get(uuid).DumpData();
        }
        //FInfo fi = FriendsManager.listFriendsInfos.get(uuid);
        //unload friends, subtract 1 from the list of needed friends data
    }

    @EventHandler
    public void OnTabCompleteEvent(TabCompleteEvent e) {

        if(!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        String prefix = e.getCursor().toLowerCase();
        List<String> args = Split(' ', prefix);

        if(args.size() == 1) {
            if(args.get(0).equals("/friend") || args.get(0).equals("/f")) {
                String[] next = new String[] {"add", "remove"};
                AddPossible(e.getSuggestions(), "", next);
            }
        } else if(args.size() == 2) {
            if(args.get(0).equals("/friend") || args.get(0).equals("/f")) {
                String[] next = new String[] {"add", "remove"};
                AddPossible(e.getSuggestions(), args.get(1), next);
            }
        } else if(args.size() == 3) {
            if(args.get(0).equals("/friend") || args.get(0).equals("/f")) {
                if(args.get(1).equals("add")) {
                    FInfo fd = HoneyNetwork.listFriendsInfos.get(player.getUniqueId());
                    if(fd != null) {
                        List<String> options = new ArrayList<String>();
                        for(ProxiedPlayer p : player.getServer().getInfo().getPlayers()) {
                            if(!p.getName().equals(player.getName()) && !fd.IsFriend(p.getUniqueId())) {
                                options.add(p.getName());
                            }
                        }
                        AddPossible(e.getSuggestions(), args.get(2), options);
                    }
                } else if(args.get(1).equals("remove")) {
                    FInfo fd = HoneyNetwork.listFriendsInfos.get(player.getUniqueId());
                    if(fd != null) {
                        List<String> options = new ArrayList<String>();
                        for(ProxiedPlayer p : player.getServer().getInfo().getPlayers()) {
                            if(!p.getName().equals(player.getName()) && fd.IsFriend(p.getUniqueId())) {
                                options.add(p.getName());
                            }
                        }
                        AddPossible(e.getSuggestions(), args.get(2), options);
                    }
                }
            }
        }
    }

    private void AddPossible(List<String> suggestions, String search, String[] options) {
        for(String option : options) {
            if(search.length() <= option.length() && search.equalsIgnoreCase(option.substring(0, search.length()))) {
                suggestions.add(option);
            }
        }
    }

    private void AddPossible(List<String> suggestions, String search, List<String> options) {
        for(String option : options) {
            if(search.length() <= option.length() && search.equalsIgnoreCase(option.substring(0, search.length()))) {
                suggestions.add(option);
            }
        }
    }

    private List<String> Split(char ch, String string) {
        String s = "";
        List<String> data = new ArrayList<String>();
        char[] array = string.toCharArray();
        for(int i = 0; i < string.length(); i++) {
            char c = array[i];
            if(c == ch) {
                data.add(s);
                s = "";
            } else {
                s += c;
            }
        }
        data.add(s);
        return data;
    }

    @EventHandler
    public void OnTabCompleteEvent2(TabCompleteEvent e) {
        String[] args = e.getCursor().split(" ");
        if(args.length == 1 || args.length == 2) {// /go play || /go .
            if(e.getSender() instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) e.getSender();
                if((args[0].equals("/kick") || args[0].equals("/ban")) && player.hasPermission("NetworkManager.admin")) {
                    String startsWith = (args.length == 1)?(""):(args[1].toLowerCase());
                    e.getSuggestions().clear();
                    for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
                        if(p.getName().toLowerCase().startsWith(startsWith) && !p.equals(player)) {
                            e.getSuggestions().add(p.getName());
                        }
                    }
                    Collections.sort(e.getSuggestions());
                }
            }
        }
    }
}
