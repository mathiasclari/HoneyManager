package fun.mcbee.bungee.honeynetwork.Util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class Utilities {

    public static TextComponent GetBText(String message) {
        String color = "0123456789abcdef";
        String exception = "klmnor";
        String newMessage = "";
        String cPrifix = "";
        boolean isPrifix = false;
        char[] seq = message.toCharArray();

        for(int i = 0; i < seq.length; i++) {
            char c = seq[i];
            if(isPrifix) {
                char lc = Character.toLowerCase(seq[i]);
                if(color.indexOf(lc) > 0) {
                    cPrifix = "§" + lc;
                } else if(exception.indexOf(lc) > 0) {
                    cPrifix += "§" + lc;
                }
            }
            isPrifix = c == '§';
            if(c == ' ') {
                newMessage += " " + cPrifix;
            } else {
                newMessage += c;
            }
        }
        return new TextComponent(newMessage);
    }

    public static boolean IsInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception e) {}
        return false;
    }

    public static TextComponent GetBText(ChatColor color, String text) {
        TextComponent tc = new TextComponent(text);
        if(color != null) {
            tc.setColor(color);
        }
        return tc;
    }
}
