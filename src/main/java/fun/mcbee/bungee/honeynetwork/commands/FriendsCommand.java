package fun.mcbee.bungee.honeynetwork.commands;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import fun.mcbee.bungee.honeynetwork.data.FInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class FriendsCommand extends Command {

    public FriendsCommand() {
        super("friends");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            int len = args.length;
            if (len == 0) {
                FInfo fi = HoneyNetwork.listFriendsInfos.get(player.getUniqueId());
                if (fi != null) {
                    fi.DisplayFriendList(player, 0);
                }
            }
        }
    }
}