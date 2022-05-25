package fun.mcbee.bungee.honeynetwork.commands;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import fun.mcbee.bungee.honeynetwork.Util.ChatUtilities;
import fun.mcbee.bungee.honeynetwork.Util.Utilities;
import fun.mcbee.bungee.honeynetwork.data.FInfo;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.awt.*;
import java.util.UUID;

public class FriendCommand extends Command {

    private String cmdName;

    public ChatColor mainColor = ChatColor.of(new Color(255, 132, 0));
    public ChatColor darkColor = ChatColor.of(new Color(232, 81, 12));
    public ChatColor errorColor = ChatColor.of(new Color(227, 7, 58));

    public FriendCommand(String cmdName) {
        super(cmdName);
        this.cmdName = cmdName;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            int len = args.length;
            if(len == 0) {
                FInfo fi = HoneyNetwork.listFriendsInfos.get(player.getUniqueId());
                if(fi != null) {
                    fi.DisplayFriendList(player, 0);
                }
            } else if(len == 1) {
                if(args[0].equalsIgnoreCase("help")) {
                    DisplayHelp(player, cmdName);
                }
            } else if(len == 2) {
                if(args[0].equalsIgnoreCase("add")) {
                    if(player.getName().equals(args[1])) {
                        player.sendMessage(GetBText(errorColor, "You can't friend yourself!"));
                        return;
                    }
                    ProxiedPlayer p = BungeeCord.getInstance().getPlayer(args[1]);
                    if(p != null) {
                        FInfo fi = HoneyNetwork.listFriendsInfos.get(player.getUniqueId());
                        FInfo fiOther = HoneyNetwork.listFriendsInfos.get(p.getUniqueId());
                        if(!fi.IsFriend(fiOther)) {
                            if(!fiOther.IsBlocked(fi)) {
                                if(fi.CanAddFriend(p)) {
                                    if(fi.AskFriend(player, p, fiOther)) {
                                        player.sendMessage(GetBText(mainColor, "Request sent to " + args[1]));
                                    } else {
                                        player.sendMessage(GetBText(errorColor, "Request already sent!"));
                                    }
                                } else {
                                    player.sendMessage(GetBText(errorColor, "You have reached your friend limit!"));
                                }
                            } else {
                                player.sendMessage(GetBText(errorColor, args[1] + " blocked you!"));
                            }
                        } else {
                            player.sendMessage(GetBText(errorColor, args[1] + " is already your friend!"));
                        }
                    } else {
                        player.sendMessage(GetBText(errorColor, args[1] + " isn't online!"));
                    }
                } else if(args[0].equalsIgnoreCase("remove")) {
                    FInfo fi = HoneyNetwork.listFriendsInfos.get(player.getUniqueId());
                    if(fi.RemoveFriend(args[1])) {
                        player.sendMessage(GetBText(mainColor, "Friend removed!"));
                    } else {
                        player.sendMessage(GetBText(errorColor, "Unable to remove " + args[1] + "!"));
                    }
                } else if(args[0].equalsIgnoreCase("accept")) {
                    FInfo fi = HoneyNetwork.listFriendsInfos.get(player.getUniqueId());
                    UUID uuid = UUID.fromString(args[1]);
                    if(uuid != null) {
                        FInfo fiOther = HoneyNetwork.listFriendsInfos.get(uuid);
                        if(!fi.AcceptRequest(player, fiOther)) {
                            player.sendMessage(GetBText(errorColor, "Request can't be found!"));
                        }
                    } else {
                        player.sendMessage(GetBText(errorColor, "Incorrect command!"));
                    }
                } else if(args[0].equalsIgnoreCase("deny")) {
                    FInfo fi = HoneyNetwork.listFriendsInfos.get(player.getUniqueId());
                    UUID uuid = UUID.fromString(args[1]);
                    if(uuid != null) {
                        if(!fi.DenyRequest(uuid)) {
                            player.sendMessage(GetBText(errorColor, "Request can't be found!"));
                        } else {
                            player.sendMessage(GetBText(mainColor, "Request denied!"));
                        }
                    } else {
                        player.sendMessage(GetBText(errorColor, "Incorrect command!"));
                    }
                } else if(args[0].equalsIgnoreCase("list")) {
                    int page = 0;
                    if(Utilities.IsInteger(args[1])) {
                        page = Integer.parseInt(args[1]);
                    }
                    FInfo fi = HoneyNetwork.listFriendsInfos.get(player.getUniqueId());
                    if(fi != null) {
                        fi.DisplayFriendList(player, page);
                    }
                }
            }
        }
    }

    private void DisplayHelp(ProxiedPlayer player, String command) {
        String textTitle = "Help Friends Commands";
        String spacingCenterTitle = ChatUtilities.GenerateText(' ', ChatUtilities.GetCenterSpacing(textTitle, ' ', ChatUtilities.GetChatLength()));
        player.sendMessage(GetBText(darkColor, spacingCenterTitle + textTitle));

        net.md_5.bungee.api.chat.TextComponent t0 = GetBText(mainColor, "/" + command + " ");
        t0.addExtra(GetBText(ChatColor.WHITE, "- Display your friends list."));
        player.sendMessage(t0);
        net.md_5.bungee.api.chat.TextComponent t1 = GetBText(mainColor, "/" + command + " add <player> ");
        t1.addExtra(GetBText(ChatColor.WHITE, "- Sends a friend request."));
        player.sendMessage(t1);
        net.md_5.bungee.api.chat.TextComponent t2 = GetBText(mainColor, "/" + command + " remove <player> ");
        t2.addExtra(GetBText(ChatColor.WHITE, "- Removes a friend from your list."));
        player.sendMessage(t2);
    }

    private net.md_5.bungee.api.chat.TextComponent GetBText(ChatColor color, String text) {
        net.md_5.bungee.api.chat.TextComponent tc = new TextComponent(text);
        if(color != null) {
            tc.setColor(color);
        }
        return tc;
    }

}

