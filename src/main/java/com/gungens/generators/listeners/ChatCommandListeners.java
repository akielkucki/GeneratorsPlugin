package com.gungens.generators.listeners;

import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.models.CommandState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatCommandListeners implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String uuid = event.getPlayer().getUniqueId().toString();
        if (GeneratorCache.instance.isInCommandQueue(uuid)) {

            event.setCancelled(true);
            String message = event.getMessage();
            try {
                double interval = Double.parseDouble(message);
                GeneratorCache.instance.setPlayerCommandQueueState(uuid, CommandState.EXECUTED,interval);
                GeneratorCache.instance.removePlayerFromQueue(uuid);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a valid number");
            }
        }
    }
}
