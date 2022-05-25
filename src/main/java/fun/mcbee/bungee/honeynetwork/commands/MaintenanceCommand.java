package fun.mcbee.bungee.honeynetwork.commands;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MaintenanceCommand extends Command {

    public MaintenanceCommand() {
        super("maintenance");
    }

    public void execute(CommandSender sender, String[] args) {
        if ((sender instanceof ProxiedPlayer)) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if (p.hasPermission("bee.superadmin")) {
                HoneyNetwork.maintenance = !HoneyNetwork.maintenance;
                HoneyNetwork.getInstance().config.set("maintenance", HoneyNetwork.maintenance);
                //QueenBeeNetwork.getInstance().saveDefaultConfig();
                if (HoneyNetwork.maintenance == true) {
                    p.sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("§8§l[§c§lBEE§8§l] §7§l» §cMaintenance has been enabled").create());
                }else {
                    p.sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("§8§l[§c§lBEE§8§l] §7§l» §cMaintenance has been disabled").create());
                }
            }


        }
    }
}
