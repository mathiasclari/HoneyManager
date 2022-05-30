package fun.mcbee.bungee.honeynetwork.listener;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeSettings implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPing(ProxyPingEvent e){
        ServerPing serverPing = e.getResponse();
        serverPing.setDescription(ChatColor.of("#FFE29F")+"       Welcome to the"+ChatColor.BOLD+" McBee Network"+ChatColor.of("#FFE29F")+" !"+"\n"+ChatColor.of("#755a3e")+"                "+ChatColor.BOLD+"play.acticraft.net");
        serverPing.getPlayers().setMax(2000);
        if(HoneyNetwork.maintenance == true) {
            serverPing.setVersion(new ServerPing.Protocol("Under Maintenance", 1));
        }else{
            ServerPing.Protocol pro = new ServerPing.Protocol(ChatColor.GOLD+"discord.gg/McBee", e.getConnection().getVersion());
            e.getResponse().setVersion(pro);
        }
        e.setResponse(serverPing);


    }

    @EventHandler
    public void postLogin(PostLoginEvent e){
        if(HoneyNetwork.maintenance == true){
            if(!e.getPlayer().hasPermission("acs.staff.maintenance")) {
                e.getPlayer().disconnect(ChatColor.of("#FFE29F") + "       Server is currently under" + ChatColor.BOLD + " Maintenance" + ChatColor.of("#FFE29F") + " !" + "\n" + ChatColor.of("#755a3e") + " " + ChatColor.BOLD + "Join our Discord for more info! discord.gg/acticraft");
            }

        }
    }

}
