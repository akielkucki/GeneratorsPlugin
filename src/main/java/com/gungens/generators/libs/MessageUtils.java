package com.gungens.generators.libs;

import org.bukkit.ChatColor;

public class MessageUtils {
    public static final MessageUtils instance = new MessageUtils();
    public String format(String textToFormat) {
        return ChatColor.translateAlternateColorCodes('&', textToFormat);
    }

    public String capitalizeFirstLetter(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
