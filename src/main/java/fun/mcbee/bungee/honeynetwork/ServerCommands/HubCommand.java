package fun.mcbee.bungee.honeynetwork.ServerCommands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HubCommand extends Command {

    public HubCommand() {
        super("hub");
    }
    public void execute(CommandSender sender, String[] args) {
        if ((sender instanceof ProxiedPlayer)) {
            ProxiedPlayer p = (ProxiedPlayer)sender;
            p.sendMessage(new ComponentBuilder("Connecting you to the hub!").color(ChatColor.of("#FFE29F")).create());

            p.connect(ProxyServer.getInstance().getServerInfo("lobby"));


        }
    }

}
