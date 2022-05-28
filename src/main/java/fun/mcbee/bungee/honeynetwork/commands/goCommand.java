package fun.mcbee.bungee.honeynetwork.commands;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class goCommand extends Command {

    public goCommand() {
        super("go");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            if(args.length == 1) {
                String serverName = args[0];
                if(HoneyNetwork.getInstance().getProxy().getServerInfo(serverName) != null) {
                    ServerInfo server = ProxyServer.getInstance().getServerInfo(serverName);
                    Callback<ServerPing> callback = new Callback<ServerPing>() {
                        public void done(ServerPing result, Throwable error) {
                            ServerPing data = result;
                            if (data != null) {
                                ((ProxiedPlayer) sender).connect(server);
                            } else {
                                ((ProxiedPlayer) sender).sendMessage(new TextComponent("server is offline"));
                            }
                        }
                    };
                    server.ping(callback);
                } else {
                    ((ProxiedPlayer) sender).sendMessage(new TextComponent("We do not have a server named this"));
                }
            } else {
                ((ProxiedPlayer) sender).sendMessage(new TextComponent("Command /go <server>"));
            }
        }
    }
}
