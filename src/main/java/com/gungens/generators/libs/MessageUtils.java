package com.gungens.generators.libs;

import org.bukkit.ChatColor;

public class MessageUtils {
    public static MessageUtils instance = new MessageUtils();
    public String format(String textToFormat) {
        return ChatColor.translateAlternateColorCodes('&', textToFormat);
    }
}
