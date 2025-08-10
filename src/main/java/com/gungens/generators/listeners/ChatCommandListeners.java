package com.gungens.generators.listeners;

import com.gungens.generators.Generators;
import com.gungens.generators.cache.BreakableGeneratorCache;
import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.libs.InventoryBuilder;
import com.gungens.generators.libs.MessageUtils;
import com.gungens.generators.libs.Register;
import com.gungens.generators.models.BreakableGenerator;
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
        MessageUtils utils = new MessageUtils();
        if (GeneratorCache.instance.isInCommandQueue(uuid)) {

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
        } else if (BreakableGeneratorCache.instance.isInCommandQueue(uuid)) {
            event.setCancelled(true);
            String message = event.getMessage();
            try {
                if (BreakableGeneratorCache.instance.getPlayerCommandQueueState(uuid).getCommandName().equals("SET_HEALTH")) {
                    setHealth(message, uuid, player, utils);
                } else if (BreakableGeneratorCache.instance.getPlayerCommandQueueState(uuid).getCommandName().equals("SET_RESET_TIME")) {
                    setResetTime(message, uuid, player, utils);
                }

            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a valid number");
            }
        }
    }

    private void setResetTime(String message, String uuid, Player player, MessageUtils utils) {
        double resetTime = Double.parseDouble(message);
        BreakableGeneratorCache.instance.setPlayerCommandQueueState(uuid, CommandState.EXECUTED,resetTime);
        InventoryBuilder inventoryBuilder = BreakableGeneratorCache.instance.getCurrentInventoryTrack(uuid);

        String genId = BreakableGeneratorCache.instance.getCurrentGeneratorTrack(uuid);
        BreakableGenerator generator = BreakableGeneratorCache.instance.getGeneratorById(genId);
        generator.setResetTime(resetTime);
        BreakableGeneratorCache.instance.updateGenerator(generator);

        BreakableGeneratorCache.instance.removePlayerFromQueue(uuid);

        player.sendMessage(utils.format("&aReset Time set to &d" + generator.getResetTime() + "&a"));
        Bukkit.getScheduler().runTask(Generators.instance, () -> {

            ItemStack clock = inventoryBuilder.getItem(27+5);
            ItemMeta meta = clock.getItemMeta();
            meta.setDisplayName(utils.format("&dRESET TIME: "+generator.getResetTime()));
            clock.setItemMeta(meta);

            player.openInventory(inventoryBuilder.build());
        });
    }

    private static void setHealth(String message, String uuid, Player player, MessageUtils utils) {
        double health = Double.parseDouble(message);
        BreakableGeneratorCache.instance.setPlayerCommandQueueState(uuid, CommandState.EXECUTED,health);
        InventoryBuilder inventoryBuilder = BreakableGeneratorCache.instance.getCurrentInventoryTrack(uuid);

        String genId = BreakableGeneratorCache.instance.getCurrentGeneratorTrack(uuid);
        BreakableGenerator generator = BreakableGeneratorCache.instance.getGeneratorById(genId);
        generator.setMaxHealth(health);
        generator.setHealth(health);
        BreakableGeneratorCache.instance.updateGenerator(generator);

        BreakableGeneratorCache.instance.removePlayerFromQueue(uuid);

        player.sendMessage(utils.format("&aMax HP set to &d" + generator.getMaxHealth() + "&a"));
        Bukkit.getScheduler().runTask(Generators.instance, () -> {

            ItemStack clock = inventoryBuilder.getItem(27+5);
            ItemMeta meta = clock.getItemMeta();
            meta.setDisplayName(utils.format("&cSET HEALTH: "+generator.getMaxHealth()));
            clock.setItemMeta(meta);

            player.openInventory(inventoryBuilder.build());
        });
    }
}
