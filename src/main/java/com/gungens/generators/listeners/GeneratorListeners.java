package com.gungens.generators.listeners;

import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.libs.InventoryBuilder;
import com.gungens.generators.libs.ItemUtils;
import com.gungens.generators.libs.MessageUtils;
import com.gungens.generators.libs.Register;
import com.gungens.generators.managers.HologramManager;
import com.gungens.generators.models.Generator;
import com.gungens.generators.models.GeneratorUIItems;
import com.gungens.generators.services.GeneratorService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;

import static com.gungens.generators.libs.CentralKeys.GEN_ID;

@Register
public class GeneratorListeners implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player p = event.getPlayer();

        ItemStack item = event.getItemInHand();
        MessageUtils utils = MessageUtils.instance;

        if (item == null || item.getType() == Material.AIR) {
            p.sendMessage(utils.format("&cYou need to place an item in your hand."));
            return;
        }
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getDisplayName().toLowerCase().endsWith("generator")) {
            return;
        }
        if (!meta.getPersistentDataContainer().has(GEN_ID, PersistentDataType.STRING)) {
            p.sendMessage(utils.format("&cFailed to create generator: block is not a generator"));
            return;
        }


        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String id = container.get(GEN_ID, PersistentDataType.STRING);
        Generator generator = GeneratorCache.instance.getGeneratorById(id);
        if (generator == null) {
            Generator newGenerator = new Generator(item.getType());
            newGenerator.setLocation(block.getLocation());
            ItemStack dropItem = ItemUtils.instance.createDropItem();
            newGenerator.setDropItems(new ArrayList<>(Collections.singletonList(dropItem)));

            GeneratorCache.instance.addGenerator(newGenerator, false);
        } else {
            generator.setLocation(block.getLocation());
            GeneratorCache.instance.updateGenerator(generator);
        }
        p.getInventory().removeItem(item);
        p.sendMessage(utils.format("&aPlaced generator"));
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        if (GeneratorCache.instance.containsLocation(location)) {
            event.setCancelled(true);
            Player p = event.getPlayer();
            MessageUtils utils = MessageUtils.instance;

            GeneratorCache cache = GeneratorCache.instance;
            Generator generator = cache.getGeneratorFromLocation(location);

            GeneratorUIItems items = new GeneratorUIItems(generator);
            Inventory inventory = new InventoryBuilder(9,utils.format("&c&lREMOVE GENERATOR?"))

                    .setItem(3, items.OK, handler -> {
                        block.setType(Material.AIR);
                        ItemStack generatorBlock = ItemUtils.instance.createGeneratorItem(generator.getBlockType(), p.getName(), generator);
                        cache.removeGenerator(generator);

                        GeneratorService.getInstance().removeHologram(generator.getId(), true, true);
                        p.sendMessage(utils.format("&aRemoved generator "+generator.getId()));
                        if (p.getInventory().firstEmpty() == -1) {
                            Inventory temp = Bukkit.createInventory(null, 9, utils.format("&9Full inventory failsafe"));
                            temp.addItem(generatorBlock);
                            p.openInventory(temp);
                        } else {
                            p.getInventory().addItem(generatorBlock);
                            p.closeInventory();
                        }

                    })
                    .setItem(5, items.CANCEL, handler -> {
                        p.closeInventory();
                    })
                    .build();

            p.openInventory(inventory);
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (InventoryBuilder.handleClick(event)) {
            return;
        }
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            MessageUtils utils = MessageUtils.instance;
            Player player = event.getPlayer();

            if (GeneratorCache.instance.containsLocation(block.getLocation())) {
                if (!player.hasPermission("generators.admin")) {
                    return;
                }
                Generator generator = GeneratorCache.instance.getGeneratorFromLocation(block.getLocation());
                event.setCancelled(true);
                InventoryBuilder builder = new InventoryBuilder(9*4, utils.format("&6&lITEM EDITOR"));

                generator.getDropItems().forEach(builder::addItem);
                GeneratorUIItems items = new GeneratorUIItems(generator);
                for (int i=0;i<9;i++) {
                    builder.setItem(27+i, items.UI_ITEM, handler -> {

                    });
                }
                builder.setItem(27, items.CLOSE_WITHOUT_SAVING, handler -> player.closeInventory());
                builder.setItem(27+2, items.SET_GLOWING, handler -> {
                    generator.setGlowing(!generator.isGlowing());
                    ItemStack currentItem = handler.getCurrentItem();
                    ItemMeta meta = currentItem.getItemMeta();
                    meta.setDisplayName(utils.format("&aGLOWING: "+generator.isGlowing()));
                    currentItem.setItemMeta(meta);

                    GeneratorCache.instance.updateGenerator(generator);
                    player.sendMessage(utils.format("&aUpdated glowing state successfully"));
                });

                builder.setItem(27+3, items.SET_INTERVAL, handler -> {
                    Player p = (Player) handler.getWhoClicked();

                    GeneratorCache.instance.addPlayerToQueue(p.getUniqueId().toString(), builder, generator.getId());
                    p.sendMessage(utils.format("&eEnter how fast (in seconds) you want this generator to drop items:\n"));
                    p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 2,1);
                    p.closeInventory();
                });
                builder.setItem(27+4, items.TOGGLE_NAME, handler -> {
                    generator.setNameVisible(!generator.isNameVisible());
                    ItemStack currentItem = handler.getCurrentItem();
                    ItemMeta meta = currentItem.getItemMeta();
                    meta.setDisplayName(utils.format("&aSHOW NAME: "+generator.isNameVisible()));
                    currentItem.setItemMeta(meta);

                    GeneratorCache.instance.updateGenerator(generator);
                    player.sendMessage(utils.format("&aUpdated show name state successfully"));
                });
                builder.setItem(27+5, items.TOGGLE_HOLO, handler -> {
                    generator.setHologramVisible(!generator.isHologramVisible());
                    HologramManager.instance.setGeneratorVisuals(generator.getId(), generator.isHologramVisible());
                    ItemStack currentItem = handler.getCurrentItem();
                    ItemMeta meta = currentItem.getItemMeta();
                    meta.setDisplayName(utils.format("&aHOLOGRAM: "+generator.isHologramVisible()));
                    currentItem.setItemMeta(meta);

                    GeneratorCache.instance.updateGenerator(generator);
                    player.sendMessage(utils.format("&aUpdated hologram state successfully"));
                });


                builder.setItem(35, items.SAVE_AND_CLOSE, handler -> {
                    Inventory inventory = handler.getInventory();
                    generator.getDropItems().clear();
                    for (int i=0;i<9*3;i++) {
                        if (builder.isInRegisteredInventories(inventory)) {
                            ItemStack stack = inventory.getItem(i);
                            if (stack != null && stack.getType() != Material.AIR) {
                                generator.addItemToDrop(stack);
                                player.sendMessage(utils.format("&aAdded item " + stack.getType().name() + " to generator " + generator.getId()));
                            }
                        } else {
                            player.sendMessage(utils.format("&cWrong inventory found"));
                        }
                    }

                    GeneratorCache.instance.updateGenerator(generator);
                    player.sendMessage(utils.format("&aUpdated generator successfully"));
                });
                Inventory inventory = builder.build();

                player.openInventory(inventory);
            }
        }
    }
}
