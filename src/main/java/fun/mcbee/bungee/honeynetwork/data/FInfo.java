package fun.mcbee.bungee.honeynetwork.data;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import fun.mcbee.bungee.honeynetwork.Util.Utilities;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;


import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;

public class FInfo {

    private List<UUID> friendsUUIDs = new ArrayList<UUID>();
    private Set<UUID> blockedUUIDs = new HashSet<UUID>();
    Map<UUID, RequestData> requestsData = new HashMap<UUID, RequestData>();

    private String name;
    private UUID uuid;

    public final ChatColor mainColor = ChatColor.of(new Color(255, 132, 0));
    public final ChatColor darkColor = ChatColor.of(new Color(232, 81, 12));
    public final ChatColor warningColor = ChatColor.of(new Color(245, 39, 132));
    public final ChatColor errorColor = ChatColor.of(new Color(227, 7, 58));

    public FInfo(UUID uuid, String name) {
        this.name = name;
        this.uuid = uuid;
        LoadData();
        HoneyNetwork.AddFriendConnection(uuid, name, true);
    }

    public void LoadData() {//TODO load blocked uuids
        BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
            @Override
            public void run() {
                Statement stmt = null;
                String query = "SELECT uuid2, name, status FROM FriendsManagerRelationsPD JOIN FriendsManagerPD ON uuid2 = uuid where uuid1 = '" + uuid + "';";
                try {
                    stmt = MYSQL.GetConnection().createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        String uuid2String = rs.getString("uuid2");
                        String name = rs.getString("name");
                        String status = rs.getString("status");
                        UUID uuid2 = UUID.fromString(uuid2String);
                        if(status.equals("Accepted")) {
                            friendsUUIDs.add(uuid2);
                            HoneyNetwork.AddFriendConnection(uuid2, name, false);
                        } else {
                            blockedUUIDs.add(uuid2);
                        }
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

                stmt = null;
                query = "INSERT INTO FriendsManagerPD (uuid, name) VALUES ('" + uuid + "', '" + name + "') ON DUPLICATE KEY UPDATE name = '" + name + "';";
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
    }

    public boolean AskFriend(ProxiedPlayer player, ProxiedPlayer playerTarget, FInfo friendTarget) {
        if(AcceptRequest(player, friendTarget)) {
            return true;
        }
        RemoveBlocked(friendTarget);
        if(!friendTarget.AddRequest(this)) {
            return false;
        }
        net.md_5.bungee.api.chat.TextComponent acceptPart = new net.md_5.bungee.api.chat.TextComponent(new ComponentBuilder("[Accept]").color(ChatColor.GREEN).create());
        acceptPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + this.GetUUID()));
        net.md_5.bungee.api.chat.TextComponent denyPart = new net.md_5.bungee.api.chat.TextComponent(new ComponentBuilder("[Deny]").color(ChatColor.RED).create());
        denyPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + this.GetUUID()));
        playerTarget.sendMessage(new                                            ComponentBuilder(this.GetName() + " wants to be your friend.")
                .color(mainColor).append(" ").append(acceptPart).append(" ").append(denyPart).create());
        return true;
    }

    public boolean CanAddFriend(ProxiedPlayer player) {


        //TODO

        return true;
    }

    private boolean AddRequest(FInfo fInfoRequester) {
        if(!requestsData.containsKey(fInfoRequester.GetUUID())) {
            requestsData.put(fInfoRequester.GetUUID(), new RequestData(this, fInfoRequester));
            return true;
        }
        return false;
    }

    public void AddFriend(ProxiedPlayer player, FInfo otherFriend) {

        player.sendMessage(Utilities.GetBText(mainColor, "You are now friends with " + otherFriend.GetName()));
        ProxiedPlayer otherPlayer = BungeeCord.getInstance().getPlayer(otherFriend.GetUUID());
        if(otherPlayer != null) {
            otherPlayer.sendMessage(Utilities.GetBText(mainColor, "You are now friends with " + player.getName()));
            otherFriend.friendsUUIDs.add(this.uuid);
            otherFriend.blockedUUIDs.remove(this.uuid);
            HoneyNetwork.AddFriendConnection(uuid, name, true);
        }
        this.friendsUUIDs.add(otherFriend.uuid);
        this.blockedUUIDs.remove(otherFriend.uuid);
        HoneyNetwork.AddFriendConnection(otherFriend.uuid, otherFriend.name, true);

        String uuid1 = this.GetUUID().toString();
        String uuid2 = otherFriend.GetUUID().toString();
        BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
            @Override
            public void run() {
                Statement stmt = null;
                String query1 = "INSERT INTO FriendsManagerRelationsPD (uuid1, uuid2, status) VALUES ('" + uuid1 + "', '" + uuid2 + "', 'Accepted') ON DUPLICATE KEY UPDATE status = 'Accepted';";
                String query2 = "INSERT INTO FriendsManagerRelationsPD (uuid1, uuid2, status) VALUES ('" + uuid2 + "', '" + uuid1 + "', 'Accepted') ON DUPLICATE KEY UPDATE status = 'Accepted';";
                try {
                    stmt = MYSQL.GetConnection().createStatement();
                    stmt.addBatch(query1);
                    stmt.addBatch(query2);
                    stmt.executeBatch();
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

    public boolean RemoveFriend(String friendName) {
        UUID uuid = null;
        for(UUID uid : this.friendsUUIDs) {
            FData fd = HoneyNetwork.listFriendData.get(uid);
            if(fd != null && fd.GetName().equals(friendName)) {
                uuid = uid;
            }
        }
        if(uuid != null) {
            return RemoveFriend(uuid);
        }
        return false;
    }

    public boolean RemoveFriend(UUID friendUUID) {
        FInfo friendInfo = HoneyNetwork.listFriendsInfos.get(friendUUID);
        if(friendInfo != null) {
            friendInfo.friendsUUIDs.remove(this.uuid);
            HoneyNetwork.RemoveFriendConnections(this.uuid);
        }
        this.friendsUUIDs.remove(friendUUID);
        HoneyNetwork.RemoveFriendConnections(friendUUID);
        String uuid1 = this.GetUUID().toString();
        String uuid2 = friendUUID.toString();
        BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
            @Override
            public void run() {
                Statement stmt = null;
                String query = "DELETE FROM FriendsManagerRelationsPD WHERE uuid1 = '" + uuid1 + "' && uuid2 = '" + uuid2 + "' || uuid1 = '" + uuid2 + "' && uuid2 = '" + uuid1 + "';";
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
        return true;
    }

    public boolean DenyRequest(UUID uuid) {
        RequestData request = requestsData.get(uuid);
        if(request != null) {
            request.Discpose(false);
            if(!blockedUUIDs.contains(uuid)) {
                blockedUUIDs.add(uuid);
                String uuid1 = this.GetUUID().toString();
                String uuid2 = uuid.toString();
                BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        Statement stmt = null;
                        String query1 = "INSERT INTO FriendsManagerRelationsPD (uuid1, uuid2, status) VALUES ('" + uuid1 + "', '" + uuid2 + "', 'Denied') ON DUPLICATE KEY UPDATE status = 'Denied';";
                        String query2 = "DELETE FROM FriendsManagerRelationsPD WHERE uuid1 = '" + uuid2 + "' and uuid2 = '" + uuid1 + "';";
                        try {
                            stmt = MYSQL.GetConnection().createStatement();
                            stmt.addBatch(query1);
                            stmt.addBatch(query2);
                            stmt.executeBatch();
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
                return true;
            }
        }
        return false;
    }

    private void RemoveBlocked(FInfo friendTarget) {
        if(blockedUUIDs.contains(uuid)) {
            blockedUUIDs.remove(uuid);
            String uuid1 = this.GetUUID().toString();
            String uuid2 = uuid.toString();
            BungeeCord.getInstance().getScheduler().runAsync(HoneyNetwork.getInstance(), new Runnable() {
                @Override
                public void run() {
                    Statement stmt = null;
                    String query = "DELETE FROM FriendsManagerRelationsPD WHERE uuid1 = '" + uuid1 + "' && uuid2 = '" + uuid2 + "';";
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
        }
    }

    public boolean AcceptRequest(ProxiedPlayer player, FInfo friendTarget) {
        RequestData request = requestsData.get(friendTarget.GetUUID());
        if(request != null) {
            request.Discpose(false);
            AddFriend(player, friendTarget);
            return true;
        }
        return false;
    }

    public UUID GetUUID() {
        return this.uuid;
    }

    public String GetName() {
        return this.name;
    }

    /**
     * DisplayFriendList - Displays players friend list.
     * @param page - Which page to display.
     * @return Returns false if there isn't a next page to display (page + 1).
     */
    public boolean DisplayFriendList(ProxiedPlayer player, int page) {
        int max = 6;

        net.md_5.bungee.api.chat.TextComponent line = new net.md_5.bungee.api.chat.TextComponent("———————————————————————————————————");
        line.setColor(ChatColor.DARK_GRAY);
        net.md_5.bungee.api.chat.TextComponent titleLeft = new net.md_5.bungee.api.chat.TextComponent("Friends List");
        titleLeft.setColor(darkColor);
        net.md_5.bungee.api.chat.TextComponent titleRight = new net.md_5.bungee.api.chat.TextComponent("Page " + (page + 1));
        titleRight.setColor(darkColor);
        net.md_5.bungee.api.chat.TextComponent titleMiddle = new net.md_5.bungee.api.chat.TextComponent(" - ");
        titleMiddle.setColor(ChatColor.GRAY);

        net.md_5.bungee.api.chat.TextComponent title = new net.md_5.bungee.api.chat.TextComponent();
        title.addExtra(titleLeft);
        title.addExtra(titleMiddle);
        title.addExtra(titleRight);

        player.sendMessage(line);

        player.sendMessage(title);

        for(int i = page * max; i < (page + 1) * max && i < friendsUUIDs.size(); i++) {
            UUID uuidFriend = friendsUUIDs.get(i);
            FData fd = HoneyNetwork.listFriendData.get(uuidFriend);
            if(fd != null) {

                net.md_5.bungee.api.chat.TextComponent message1 = new net.md_5.bungee.api.chat.TextComponent("» " + fd.GetName() + " ");
                message1.setColor(mainColor);
                net.md_5.bungee.api.chat.TextComponent message2 = new net.md_5.bungee.api.chat.TextComponent("●");
                ProxiedPlayer pd = BungeeCord.getInstance().getPlayer(uuidFriend);
                if(pd != null) {
                    message2.setColor(ChatColor.GREEN);
                    message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Player Online!" )));
                } else {
                    message2.setColor(ChatColor.RED);
                    message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Player Offline!")));
                }
                net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent();
                message.addExtra(message1);
                message.addExtra(message2);

                player.sendMessage(message);

            }
        }

        net.md_5.bungee.api.chat.TextComponent pageLeft = new net.md_5.bungee.api.chat.TextComponent("<<");
        pageLeft.setColor(ChatColor.YELLOW);
        if(page > 0) {
            pageLeft.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Back")));
            pageLeft.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend " + (page - 1)));
        }
        net.md_5.bungee.api.chat.TextComponent pageRight = new net.md_5.bungee.api.chat.TextComponent(">>");
        pageRight.setColor(ChatColor.YELLOW);
        if(page < friendsUUIDs.size()/max) {
            pageLeft.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Back")));
            pageLeft.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend " + (page + 1)));
        }
        net.md_5.bungee.api.chat.TextComponent pageMiddle = new net.md_5.bungee.api.chat.TextComponent("             -             ");
        pageMiddle.setColor(ChatColor.GRAY);

        net.md_5.bungee.api.chat.TextComponent pageMessage = new net.md_5.bungee.api.chat.TextComponent();
        pageMessage.addExtra(pageLeft);
        pageMessage.addExtra(pageMiddle);
        pageMessage.addExtra(pageRight);

        player.sendMessage(pageMessage);

        player.sendMessage(line);

        return friendsUUIDs.size() > (page + 1) * max;
    }

    public void DumpData() {
        HoneyNetwork.RemoveFriendConnections(friendsUUIDs);
        HoneyNetwork.listFriendsInfos.remove(this.uuid);
        HoneyNetwork.RemoveFriendConnections(this.uuid);
    }

    public boolean IsFriend(FInfo fi) {
        return IsFriend(fi.GetUUID());
    }

    public boolean IsFriend(UUID uuid) {
        return this.friendsUUIDs.contains(uuid);
    }

    public boolean IsBlocked(FInfo fi) {
        return IsBlocked(fi.GetUUID());
    }

    public boolean IsBlocked(UUID uuid) {
        return this.blockedUUIDs.contains(uuid);
    }

    public void SendLogMessage(ProxiedPlayer player, String message) {
        for(UUID uuid : this.friendsUUIDs) {
            ProxiedPlayer p = BungeeCord.getInstance().getPlayer(uuid);
            if(p != null) {
                p.sendMessage(new TextComponent(message));
            }
        }
    }

}

