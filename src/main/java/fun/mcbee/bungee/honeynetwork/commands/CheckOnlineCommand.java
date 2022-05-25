package fun.mcbee.bungee.honeynetwork.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CheckOnlineCommand extends Command {

    public CheckOnlineCommand() {
        super("checkonline");
    }

    public void execute(CommandSender sender, String[] args) {
        if ((sender instanceof ProxiedPlayer)) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if (p.hasPermission("bee.bungee.admin")) {
                p.sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("§8§l[§6§lMCBEE§8§l] §7§l» §6" + ProxyServer.getInstance().getOnlineCount()).create());

            }




        }
    }
}
