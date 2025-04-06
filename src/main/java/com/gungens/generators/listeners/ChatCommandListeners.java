package com.gungens.generators.listeners;

import com.gungens.generators.Generators;
import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.libs.InventoryBuilder;
import com.gungens.generators.libs.MessageUtils;
import com.gungens.generators.libs.Register;
import com.gungens.generators.models.CommandState;
import com.gungens.generators.models.Generator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Register
public class ChatCommandListeners implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String uuid = event.getPlayer().getUniqueId().toString();
        if (GeneratorCache.instance.isInCommandQueue(uuid)) {
            MessageUtils utils = new MessageUtils();
            event.setCancelled(true);
            String message = event.getMessage();
            try {
                double interval = Double.parseDouble(message);
                GeneratorCache.instance.setPlayerCommandQueueState(uuid, CommandState.EXECUTED,interval);
                InventoryBuilder inventoryBuilder = GeneratorCache.instance.getCurrentInventoryTrack(uuid);

                String genId = GeneratorCache.instance.getCurrentGeneratorTrack(uuid);
                Generator generator = GeneratorCache.instance.getGeneratorById(genId);
                generator.setTickTime(interval);
                GeneratorCache.instance.updateGenerator(generator);

                GeneratorCache.instance.removePlayerFromQueue(uuid);
                player.sendMessage(utils.format("&aInterval set to &d" + generator.getTickTime() + "&a"));
                Bukkit.getScheduler().runTask(Generators.instance, () -> {

                    ItemStack clock = inventoryBuilder.getItem(27+4);
                    ItemMeta meta = clock.getItemMeta();
                    meta.setDisplayName(utils.format("&dINTERVAL: "+generator.getTickTime()));
                    clock.setItemMeta(meta);

                    player.openInventory(inventoryBuilder.build());
                });

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a valid number");
            }
        }
    }
}
