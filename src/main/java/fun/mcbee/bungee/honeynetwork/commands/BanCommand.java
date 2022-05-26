package fun.mcbee.bungee.honeynetwork.commands;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import fun.mcbee.bungee.honeynetwork.data.MYSQL;
import fun.mcbee.bungee.honeynetwork.listener.PluginMessaging;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BanCommand extends Command {

    public BanCommand() {
        super("ban");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if(player.hasPermission("NetworkManager.admin")) {
                if(args.length == 0) {
                    TextComponent tc = new TextComponent("Ussage: /ban [username/UUID] <perma/1Y,2M,3D,4h,5m,6s> <reason>");
                    tc.setColor(HoneyNetwork.errorColor);
                    player.sendMessage(tc);
                } else {
                    String id = args[0];
                    if(id.length() == 36) {
                        ProxiedPlayer playerSelected = BungeeCord.getInstance().getPlayer(UUID.fromString(id));
                        if(playerSelected != null) {
                            InetSocketAddress sa = (InetSocketAddress) playerSelected.getSocketAddress();
                            String ip = sa.getAddress().toString().split("/")[1];
                            RunBanCommand(args, player, playerSelected.getUniqueId(), playerSelected.getName(), ip);
                        } else {//Selected player can't be found, search in database
                            BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    Statement stmt = null;
                                    String query = "SELECT * FROM FriendsManagerPD WHERE uuid = '" + id + "';";
                                    try {
                                        stmt = MYSQL.GetConnection().createStatement();
                                        ResultSet rs = stmt.executeQuery(query);
                                        UUID uuid = null;
                                        String name = null;
                                        while (rs.next()) {
                                            uuid = UUID.fromString(rs.getString("uuid"));
                                            name = rs.getString("name");
                                            break;
                                        }
                                        if(uuid != null) {
                                            RunBanCommand(args, player, uuid, name, "");
                                        } else {
                                            TextComponent tc = new TextComponent("User never joied the network!");
                                            tc.setColor(HoneyNetwork.errorColor);
                                            player.sendMessage(tc);
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
                    } else {
                        ProxiedPlayer playerSelected = BungeeCord.getInstance().getPlayer(id);
                        if(playerSelected != null) {
                            InetSocketAddress sa = (InetSocketAddress) playerSelected.getSocketAddress();
                            String ip = sa.getAddress().toString().split("/")[1];
                            RunBanCommand(args, player, playerSelected.getUniqueId(), playerSelected.getName(), ip);
                        } else { //Selected player can't be found, search in database
                            BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    Statement stmt = null;
                                    String query = "SELECT * FROM FriendsManagerPD WHERE name = '" + id + "';";
                                    try {
                                        stmt = MYSQL.GetConnection().createStatement();
                                        ResultSet rs = stmt.executeQuery(query);
                                        UUID uuid = null;
                                        String name = null;
                                        while (rs.next()) {
                                            uuid = UUID.fromString(rs.getString("uuid"));
                                            name = rs.getString("name");
                                            break;
                                        }
                                        if(uuid != null) {
                                            RunBanCommand(args, player, uuid, name, "");
                                        } else {
                                            TextComponent tc = new TextComponent("User never joied the network!");
                                            tc.setColor(HoneyNetwork.errorColor);
                                            player.sendMessage(tc);
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
                    }
                }
            }
        }
    }

    private void RunBanCommand(String[] args, ProxiedPlayer player, UUID uuid, String name, String ip) {
        if(args.length == 1) {
            OpenBanMenu(player, uuid, name, ip);
        } else {
            Long time = GetTimeFromString(args[1]);
            if(time != null) {
                String reason = "";
                for(int i = 2; i < args.length; i++) {
                    if(i > 2) {
                        reason += " ";
                    }
                    reason += args[i];
                }
                BanUser(player, uuid, time, reason, ip);
            } else {
                TextComponent tc = new TextComponent("Wrong time format for the ban command!");
                tc.setColor(HoneyNetwork.errorColor);
                player.sendMessage(tc);
            }
        }
    }

    private Long GetTimeFromString(String timeInString) {
        if(timeInString.equals("perma")) {
            return -1l;
        }
        String[] timeFrames = timeInString.split(",");
        int years = 0;
        int months = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        try {
            for(String frame : timeFrames) {//1Y,2M,3D,4h,5m,6s
                String numberText = frame.substring(0, frame.length() - 1);
                int number = Integer.parseInt(numberText);
                char c = frame.charAt(frame.length() - 1);
                if(c == 'Y') {
                    years += number;
                } else if(c == 'M') {
                    months += number;
                } else if(c == 'D') {
                    days += number;
                } else if(c == 'h') {
                    hours += number;
                } else if(c == 'm') {
                    minutes += number;
                } else if(c == 's') {
                    seconds += number;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }
        long time = 31556926000l * years + 2629743830l * months + 86400000 * days + 3600000 * hours + 60000 * minutes + 1000 * seconds;
        if(time != 0) {
            return time;
        }
        return null;
    }

    public void OpenBanMenu(ProxiedPlayer player, UUID uuid, String name, String ip) {
        PluginMessaging.sendToBukkit("open_ban_menu", player.getUniqueId().toString() + ":" + uuid + ":" + name + ":" + ip, player.getServer().getInfo());
    }

    public static void BanUser(ProxiedPlayer player, UUID uuid, Long time, String reason, String ip) {
        Long finalTime = time + System.currentTimeMillis();
        HoneyNetwork.bannedPlayers.put(uuid, finalTime);
        BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
            @Override
            public void run() {
                Statement stmt = null;
                String realReason = MYSQL.mysql_real_escape_string(reason);
                String query = "INSERT INTO NetworkManagerBans (uuid, time_ban_expires, banned_ip, reason) "
                        + "VALUES ('" + uuid + "', " + finalTime + ", INET_ATON('" + ip + "'), '" + realReason + "') "
                        + "ON DUPLICATE KEY UPDATE time_ban_expires = " + finalTime + ", banned_ip = INET_ATON('" + ip + "'),"
                        + " banned_times = banned_times + 1, reason = '" + realReason + "', banned_date = now();";
                try {
                    stmt = MYSQL.GetConnection().createStatement();
                    stmt.executeUpdate(query);
                    TextComponent tc = new TextComponent("Player has been successfully banned!");
                    tc.setColor(HoneyNetwork.successColor);
                    player.sendMessage(tc);
                    ProxiedPlayer playerToBeBanned = BungeeCord.getInstance().getPlayer(uuid);
                    if(playerToBeBanned != null) {
                        TextComponent kickTitle = new TextComponent("You have been banned from the server!\n");
                        kickTitle.setColor(HoneyNetwork.errorColor);
                        kickTitle.setBold(true);

                        TextComponent message;

                        if(finalTime <= -1) {
                            message = new TextComponent("You are permanently banned from the McBee!");
                        } else {
                            String time = BanCommand.GetTextTime((finalTime - System.currentTimeMillis()) / 1000);
                            message = new TextComponent("You are banned for " + time + " from the McBee!");
                        }
                        message.setColor(ChatColor.of("#FFBF00"));
                        TextComponent reasonMessage = new TextComponent("\n\n" + reason);
                        reasonMessage.setColor(ChatColor.of("#FAD5A5"));

                        TextComponent finalMessage = new TextComponent();
                        finalMessage.addExtra(kickTitle);
                        finalMessage.addExtra(message);
                        finalMessage.addExtra(reasonMessage);

                        playerToBeBanned.disconnect(finalMessage);
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

    public static String GetTextTime(Long t) {
        List<Integer> times = new ArrayList<Integer>();
        int years = (int)(t/31556926d);
        times.add(years);
        t = t - (long)(years * 31556926d);
        int days = (int)(t/86400d);
        times.add(days);
        t = t - (long)(days * 86400d);
        int hours = (int)(t/3600d);
        times.add(hours);
        t = t - (long)(hours * 3600d);
        int minutes = (int)(t/60d);
        times.add(minutes);
        int seconds = t.intValue() - (int)(minutes * 60d);
        times.add(seconds);
        String timeDispaly = "";
        String[] suffS = {"year", "day", "hour", "minute", "second"};
        for(int i = 0; i < times.size(); i++) {
            int time = times.get(i);
            if(time != 0) {
                String suffix = suffS[i];
                if(!timeDispaly.isEmpty()) {
                    timeDispaly += ", ";
                }
                if(time == 1) {
                    timeDispaly += time + " " + suffix;
                } else if(time > 1) {
                    timeDispaly += time + " " + suffix + "s";
                }
            }
        }
        if(timeDispaly.isEmpty()) {
            timeDispaly = "0 seconds";
        }
        return timeDispaly;
    }

}
