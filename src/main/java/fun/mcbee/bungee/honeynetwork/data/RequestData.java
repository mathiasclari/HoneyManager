package fun.mcbee.bungee.honeynetwork.data;

import fun.mcbee.bungee.honeynetwork.HoneyNetwork;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class RequestData {

    private FInfo fi;
    private FInfo requesterFi;
    private ScheduledTask task;

    public ChatColor mainColor = ChatColor.of(new Color(255, 132, 0));
    public ChatColor darkColor = ChatColor.of(new Color(232, 81, 12));
    public ChatColor warningColor = ChatColor.of(new Color(245, 39, 132));
    public ChatColor errorColor = ChatColor.of(new Color(227, 7, 58));

    public RequestData(FInfo fi, FInfo requesterFi) {
        this.fi = fi;
        this.requesterFi = requesterFi;
        task = BungeeCord.getInstance().getScheduler().schedule(HoneyNetwork.getInstance(), new Runnable() {
            @Override
            public void run() {
                Discpose(true);
            }
        }, 5, TimeUnit.MINUTES);
    }

    public void Discpose(boolean expired) {
        task.cancel();
        fi.requestsData.remove(requesterFi.GetUUID());
        if(!expired) {
            return;
        }
        ProxiedPlayer pMe = BungeeCord.getInstance().getPlayer(fi.GetUUID());
        ProxiedPlayer p2 = BungeeCord.getInstance().getPlayer(requesterFi.GetUUID());
        if(pMe != null) {
            pMe.sendMessage(GetBText(warningColor, "Request from " + requesterFi.GetName() + " has expired!"));
        }
        if(p2 != null) {
            p2.sendMessage(GetBText(warningColor, "Your request for " + fi.GetName() + "'s friendship has expired!"));
        }
    }

    public FInfo GetRequesterInfo() {
        return requesterFi;
    }

    private net.md_5.bungee.api.chat.TextComponent GetBText(ChatColor color, String text) {
        net.md_5.bungee.api.chat.TextComponent tc = new TextComponent(text);
        if(color != null) {
            tc.setColor(color);
        }
        return tc;
    }

}
