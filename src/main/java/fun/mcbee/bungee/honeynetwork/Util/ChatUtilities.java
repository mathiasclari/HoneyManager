package fun.mcbee.bungee.honeynetwork.Util;

import java.util.ArrayList;
import java.util.List;

public class ChatUtilities {
    public static int GetCharacterSize(char c) {
        int cSize = 18;
        if(c == 'i' || c == '.' || c == ',' || c == '|') {
            cSize = 6;
        } else if(c == 'l') {
            cSize = 9;
        } else if(c == 'I' || c == 't' || c == ' ' || c == '(' || c == ')') {
            cSize = 12;
        } else if(c == 'f' || c == 'k') {
            cSize = 15;
        } else if(c == '«' || c == '»') {
            cSize = 21;
        } else if(c == '✦') {
            cSize = 24;
        } else if(c == '—' || c == '█') {
            cSize = 27;
        }
        return cSize;
    }

    public static int GetStringLength(String text) {
        int sSize = 0;
        for(char c : text.toCharArray()) {
            sSize += GetCharacterSize(c);
        }
        return sSize;
    }

    public static int GetChatLength() {
        return 966;
    }

    public static String GenerateText(char c, int length) {
        String text = "";
        for(int i = 0; i < length; i++) {
            text += c;
        }
        return text;
    }

    public static String GetFullLineWithCharacter(char c) {
        int charLen = GetCharacterSize(c);
        int n = GetChatLength()/charLen;
        String text = "";
        for(int i = 0; i < n; i++) {
            text += c;
        }
        return text;
    }

    public static int GetCenterSpacing(String textToCenter, char c, int totalLength) {
        int sizeOfSpacer = GetCharacterSize(c);
        int sizeOfText2 = GetStringLength(textToCenter) / 2;
        int difference = totalLength / 2 - sizeOfText2;
        return difference / sizeOfSpacer;
    }

    public static List<String> GetLinesFromText(String text, ChatAlignment alignment) {
        List<String> lines = new ArrayList<String>();
        int spaceLen = GetCharacterSize(' ');
        int lineLen = 0;
        String[] words = text.split(" ");
        String currentLine = "";
        for(String word : words) {
            int wLen = GetStringLength(word);
            if(wLen + spaceLen > GetChatLength()) {
                if(!currentLine.equals("")) {
                    lines.add(AlignLine(currentLine, ' ', alignment));
                    currentLine = "";
                }
                lines.add(AlignLine(word, ' ', alignment));
            } else {
                if(lineLen + wLen + spaceLen >= GetChatLength()) {
                    lines.add(AlignLine(currentLine, ' ', alignment));
                    currentLine = word;
                    lineLen = 0;
                } else {
                    if(!currentLine.equals("")) {
                        currentLine += " ";
                    }
                    currentLine += word;
                    lineLen = GetStringLength(currentLine);
                }
            }
        }
        if(!currentLine.equals("")) {
            lines.add(AlignLine(currentLine, ' ', alignment));
        }
        return lines;
    }

    public static String AlignLine(String text, char c, ChatAlignment alignment) {
        if(alignment.equals(ChatAlignment.Left)) {
            return text;
        }
        int spaceLen = GetCharacterSize(c);
        int wordLine = GetStringLength(text);
        String spacing = "";
        int n = 0;
        if(alignment.equals(ChatAlignment.Right)) {
            n = (GetChatLength() - wordLine)/spaceLen;
        } else if(alignment.equals(ChatAlignment.Center)) {
            n = ((GetChatLength() - wordLine)/spaceLen)/2;
        }
        for(int i = 0; i < n; i++) {
            spacing += c;
        }
        return spacing + text;
    }
}
