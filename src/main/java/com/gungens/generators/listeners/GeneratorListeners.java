package com.gungens.generators.listeners;

import com.google.gson.Gson;
import com.gungens.generators.Generators;
import com.gungens.generators.cache.BreakableGeneratorCache;
import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.libs.*;
import com.gungens.generators.managers.HologramManager;
import com.gungens.generators.models.BreakableGenerator;
import com.gungens.generators.models.BreakableGeneratorUIItems;
import com.gungens.generators.models.Generator;
import com.gungens.generators.models.GeneratorUIItems;
import com.gungens.generators.services.GeneratorService;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
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
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;

import static com.gungens.generators.libs.CentralKeys.BREAKABLE_GEN_ID;
import static com.gungens.generators.libs.CentralKeys.GEN_ID;

@Register
public class GeneratorListeners implements Listener {

    private static final Logger log = LoggerFactory.getLogger(GeneratorListeners.class);

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
        if (!meta.getPersistentDataContainer().has(GEN_ID, PersistentDataType.STRING) && !meta.getPersistentDataContainer().has(BREAKABLE_GEN_ID, PersistentDataType.STRING)) {
            p.sendMessage(utils.format("&cFailed to create generator: block is not a generator"));
            return;
        }


        if (isDropperGenerator(item)) {
            setupDropperGenerator(item, block);
            p.getInventory().removeItem(item);
        } else {
            setupBreakableGenerator(item, block);
        }

        p.sendMessage(utils.format("&aPlaced generator"));
    }
    Gson gson = new Gson();

    private void setupDropperGenerator(ItemStack item, Block block) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String id = container.get(GEN_ID, PersistentDataType.STRING);
        Generator generator = GeneratorCache.instance.getGeneratorById(id);
        if (generator == null) {
            generator = new Generator(item.getType());
            generator.setLocation(block.getLocation());
            ItemStack dropItem = ItemUtils.instance.createDropItem();
            generator.setDropItems(new ArrayList<>(Collections.singletonList(dropItem)));
            generator.setBlockTypeName(block.getType().name());
            GeneratorCache.instance.addGenerator(generator, false);
            log.info("New generator created: {}", gson.toJson(generator, Generator.class));

        } else {
            generator.setLocation(block.getLocation());
            GeneratorCache.instance.updateGenerator(generator);
        }
        generator.setBlockType(Material.valueOf(generator.getBlockTypeName()));

    }
    private void setupBreakableGenerator(ItemStack item, Block block) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String id = container.get(BREAKABLE_GEN_ID, PersistentDataType.STRING);
        BreakableGenerator generator = BreakableGeneratorCache.instance.getBreakableGeneratorById(id);

        if (generator == null) {
            generator = new BreakableGenerator(item.getType());
            generator.setLocation(block.getLocation());
            ItemStack dropItem = ItemUtils.instance.createDropItem();
            generator.addItemToDrop(dropItem);
            generator.setBlockTypeName(block.getType().name());

            log.info("New breakable generator created: {}", gson.toJson(generator, BreakableGenerator.class));
            BreakableGeneratorCache.instance.addBreakableGenerator(generator, false);

        } else {
            generator.setLocation(block.getLocation());
            BreakableGeneratorCache.instance.updateBreakableGenerator(generator);
        }
        generator.setBlockType(Material.valueOf(generator.getBlockTypeName()));
    }

    private boolean isDropperGenerator(ItemStack item) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(GEN_ID, PersistentDataType.STRING);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Location loc = block.getLocation();

        if (GeneratorCache.instance.containsLocation(loc)) {
            event.setCancelled(true);
            if (event.getPlayer().hasPermission("generators.admin") && event.getPlayer().isSneaking()) {
                setGeneratorType(event.getPlayer().getInventory().getItemInMainHand().getType(), GeneratorCache.instance.getGeneratorFromLocation(loc), event.getPlayer());
                return;
            }
            handlePermanentGeneratorBreak(event.getPlayer(), block, loc);

            return;
        }

        if (BreakableGeneratorCache.instance.containsLocation(loc)) {
            event.setCancelled(true);
            if (event.getPlayer().hasPermission("generators.admin") && event.getPlayer().isSneaking()) {
                setGeneratorType(event.getPlayer().getInventory().getItemInMainHand().getType(), BreakableGeneratorCache.instance.getBreakableGeneratorFromLocation(loc), event.getPlayer());
                return;
            }
            handleBreakableGeneratorBreak(event.getPlayer(), block, loc);

        }
    }

    /* ============================ Permanent Generators ============================ */

    private void handlePermanentGeneratorBreak(Player p, Block block, Location loc) {
        final GeneratorCache cache = GeneratorCache.instance;
        final Generator generator = cache.getGeneratorFromLocation(loc);

        openRemovalConfirmUI(p, block, generator);
    }

    private void openRemovalConfirmUI(Player p, Block block, Generator generator) {
        final MessageUtils utils = MessageUtils.instance;
        final GeneratorUIItems items = new GeneratorUIItems(generator);

        Inventory inv = new InventoryBuilder(9, utils.format("&c&lREMOVE GENERATOR?"))
                .setItem(3, items.OK, handler -> onConfirmRemove(p, block, generator))
                .setItem(5, items.CANCEL, handler -> p.closeInventory())
                .build();

        p.openInventory(inv);
    }

    private void onConfirmRemove(Player p, Block block, Generator generator) {
        final MessageUtils utils = MessageUtils.instance;
        final GeneratorCache cache = GeneratorCache.instance;

        // remove the block + hologram + cache entry, return item to player
        block.setType(Material.AIR);

        ItemStack generatorBlock = ItemUtils.instance.createGeneratorItem(
                generator.getBlockType(), p.getName(), generator
        );

        cache.removeGenerator(generator);
        GeneratorService.getInstance().removeHologram(generator.getId(), true, true);

        p.sendMessage(utils.format("&aRemoved generator " + generator.getId()));

        if (p.getInventory().firstEmpty() == -1) {
            Inventory temp = Bukkit.createInventory(null, 9, utils.format("&9Full inventory failsafe"));
            temp.addItem(generatorBlock);
            p.openInventory(temp);
        } else {
            p.getInventory().addItem(generatorBlock);
            p.closeInventory();
        }
    }

    /* ============================ Breakable Generators ============================ */

    private void handleBreakableGeneratorBreak(Player p, Block block, Location loc) {
        final MessageUtils utils = MessageUtils.instance;
        final World world = block.getWorld();
        final BreakableGeneratorCache cache = BreakableGeneratorCache.instance;
        final BreakableGenerator gen = cache.getBreakableGeneratorFromLocation(loc);

        if (gen.getHealth() <= 0) return;

        // reward on each hit
        GeneratorService.getInstance().addItemToPlayer(gen, p);
        world.playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);

        int newHealth = (int) (gen.getHealth() - 1);
        if (newHealth > 0) {
            applyNonFinalDamage(gen);
            return;
        }

        applyFinalBreakAndScheduleReset(block, gen);
    }

    private void applyNonFinalDamage(BreakableGenerator gen) {
        gen.damage(1);
        BreakableGeneratorCache.instance.updateGenerator(gen);
        updateHealthBarAsync(gen); // reflect reduced HP
    }

    private void applyFinalBreakAndScheduleReset(Block block, BreakableGenerator gen) {
        final World world = block.getWorld();
        final Plugin plugin = Generators.instance;

        // capture original state BEFORE we touch the block
        final Material originalType = block.getType();
        final BlockData originalData = block.getBlockData().clone();

        // show “broken” state immediately
        block.setType(Material.STONE);

        // finalize to 0 HP and show it
        gen.damage(1);
        BreakableGeneratorCache.instance.updateGenerator(gen);
        updateHealthBarAsync(gen);

        // schedule reset to original after resetTime seconds
        long delayTicks = (long) (gen.getResetTime() * 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            block.setType(originalType, false);
            block.setBlockData(originalData, true);

            world.spawnParticle(
                    Particle.GUST_EMITTER_SMALL,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    2, 0.1, 0.1, 0.1, 2
            );

            gen.setHealth(gen.getMaxHealth());
            BreakableGeneratorCache.instance.updateGenerator(gen);
            GeneratorService.getInstance().spawnHealthBarIfNotPresent(gen);
        }, delayTicks);
    }

    private void updateHealthBarAsync(BreakableGenerator gen) {
        Bukkit.getScheduler().runTask(Generators.instance, () ->
                GeneratorService.getInstance().spawnHealthBarIfNotPresent(gen)
        );
    }

    public void setGeneratorType(Material material, Generator generator, Player notifiyPlayer) {
        generator.setBlockType(material);
        generator.setBlockTypeName(material.name());
        generator.getLocation().getBlock().setType(material);
        GeneratorCache.instance.updateGenerator(generator);
        notifiyPlayer.sendMessage(MessageUtils.instance.format("&aUpdated generator type to " + material.name()));
    }
    public void setGeneratorType(Material material, BreakableGenerator generator, Player notifiyPlayer) {
        generator.setBlockType(material);
        generator.setBlockTypeName(material.name());
        generator.getLocation().getBlock().setType(material);
        BreakableGeneratorCache.instance.updateGenerator(generator);
        notifiyPlayer.sendMessage(MessageUtils.instance.format("&aUpdated generator type to " + material.name()));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (InventoryBuilder.handleClick(event)) {
            return;
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Block block = event.getClickedBlock();
        if (block == null) return;

        final Player player = event.getPlayer();
        final Location loc = block.getLocation();

        if (GeneratorCache.instance.containsLocation(loc)) {
            if (!player.hasPermission("generators.admin")) return;
            event.setCancelled(true);
            handlePermanentGeneratorInteract(player, block, loc);
            return;
        }

        if (BreakableGeneratorCache.instance.containsLocation(loc)) {
            if (!player.hasPermission("generators.admin")) return;
            event.setCancelled(true);
            handleBreakableGeneratorInteract(player, block, loc);
        }
    }

    /* ============================ Permanent (non-breakable) ============================ */

    private void handlePermanentGeneratorInteract(Player player, Block block, Location loc) {
        final Generator generator = GeneratorCache.instance.getGeneratorFromLocation(loc);
        player.openInventory(buildPermanentGeneratorEditor(player, generator));
    }

    private Inventory buildPermanentGeneratorEditor(Player player, Generator generator) {
        final MessageUtils utils = MessageUtils.instance;
        final InventoryBuilder builder = new InventoryBuilder(9 * 4, utils.format("&6&lITEM EDITOR"));

        // first 3 rows = drops
        generator.getDropItems().forEach(builder::addItem);

        final GeneratorUIItems items = new GeneratorUIItems(generator);
        decorateBottomRow(builder, items.UI_ITEM);

        builder.setItem(27, items.CLOSE_WITHOUT_SAVING, h -> player.closeInventory());

        builder.setItem(27 + 2, items.SET_GLOWING, h -> {
            generator.setGlowing(!generator.isGlowing());
            relabel(h.getCurrentItem(), utils.format("&aGLOWING: " + generator.isGlowing()));
            GeneratorCache.instance.updateGenerator(generator);
            player.sendMessage(utils.format("&aUpdated glowing state successfully"));
        });

        builder.setItem(27 + 3, items.SET_INTERVAL, h -> {
            final Player p = (Player) h.getWhoClicked();
            GeneratorCache.instance.addPlayerToQueue(p.getUniqueId().toString(), builder, generator.getId(), "SET_INTERVAL");
            p.sendMessage(utils.format("&eEnter how fast (in seconds) you want this generator to drop items:\n"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 2, 1);
            p.closeInventory();
        });

        builder.setItem(27 + 4, items.TOGGLE_NAME, h -> {
            generator.setNameVisible(!generator.isNameVisible());
            relabel(h.getCurrentItem(), utils.format("&aSHOW NAME: " + generator.isNameVisible()));
            GeneratorCache.instance.updateGenerator(generator);
            player.sendMessage(utils.format("&aUpdated show name state successfully"));
        });

        builder.setItem(27 + 5, items.TOGGLE_HOLO, h -> {
            generator.setHologramVisible(!generator.isHologramVisible());
            HologramManager.instance.setGeneratorVisuals(generator.getId(), generator.isHologramVisible());
            relabel(h.getCurrentItem(), utils.format("&aHOLOGRAM: " + generator.isHologramVisible()));
            GeneratorCache.instance.updateGenerator(generator);
            player.sendMessage(utils.format("&aUpdated hologram state successfully"));
        });

        builder.setItem(35, items.SAVE_AND_CLOSE, h -> {
            saveDropItems(builder, h.getInventory(), generator, player, false);
            GeneratorCache.instance.updateGenerator(generator);
            player.sendMessage(utils.format("&aUpdated generator successfully"));
            player.closeInventory();
        });

        return builder.build();
    }

    /* ============================ Breakable ============================ */

    private void handleBreakableGeneratorInteract(Player player, Block block, Location loc) {
        final MessageUtils utils = MessageUtils.instance;
        final BreakableGenerator gen = BreakableGeneratorCache.instance.getBreakableGeneratorFromLocation(loc);

        if (player.isSneaking()) {
            // Quick remove (admin)
            player.getInventory().addItem(ItemUtils.instance.createBreakableGeneratorItem(gen.getBlockType(), player.getName(), gen));
            BreakableGeneratorCache.instance.removeBreakableGenerator(gen);
            player.sendMessage(utils.format("&aRemoved breakable generator " + gen.getId()));
            block.setType(Material.AIR);
            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            return;
        }

        player.openInventory(buildBreakableGeneratorEditor(player, gen));
    }

    private Inventory buildBreakableGeneratorEditor(Player player, BreakableGenerator generator) {
        final MessageUtils utils = MessageUtils.instance;
        final InventoryBuilder builder = new InventoryBuilder(9 * 4, utils.format("&6&lBREAKABLE GENERATOR EDITOR"));

        generator.getDropItems().forEach(builder::addItem);

        final BreakableGeneratorUIItems items = new BreakableGeneratorUIItems(generator);
        decorateBottomRow(builder, items.UI_ITEM);

        builder.setItem(27, items.CLOSE_WITHOUT_SAVING, h -> player.closeInventory());

        builder.setItem(27 + 2, items.SET_GLOWING, h -> {
            generator.setGlowing(!generator.isGlowing());
            relabel(h.getCurrentItem(), utils.format("&aGLOWING: " + generator.isGlowing()));
            BreakableGeneratorCache.instance.updateBreakableGenerator(generator);
            player.sendMessage(utils.format("&aUpdated glowing state successfully"));
        });

        builder.setItem(27 + 3, items.SET_RESET_TIME, h -> {
            final Player p = (Player) h.getWhoClicked();
            BreakableGeneratorCache.instance.addPlayerToQueue(p.getUniqueId().toString(), builder, generator.getId(), "SET_RESET_TIME");
            p.sendMessage(utils.format("&eEnter reset time (in seconds) for this breakable generator:\n"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 2, 1);
            p.closeInventory();
        });

        builder.setItem(27 + 4, items.TOGGLE_NAME, h -> {
            generator.setNameVisible(!generator.isNameVisible());
            relabel(h.getCurrentItem(), utils.format("&aSHOW NAME: " + generator.isNameVisible()));
            BreakableGeneratorCache.instance.updateBreakableGenerator(generator);
            player.sendMessage(utils.format("&aUpdated show name state successfully"));
        });

        builder.setItem(27 + 5, items.SET_HEALTH, h -> {
            // FIX: previously toggled name by mistake
            relabel(h.getCurrentItem(), utils.format("&6SET HEALTH: " + generator.getMaxHealth()));
            BreakableGeneratorCache.instance.addPlayerToQueue(player.getUniqueId().toString(), builder, generator.getId(), "SET_HEALTH");
            player.closeInventory();
        });

        builder.setItem(35, items.SAVE_AND_CLOSE, h -> {
            saveDropItems(builder, h.getInventory(), generator, player, true);
            BreakableGeneratorCache.instance.updateBreakableGenerator(generator);
            player.sendMessage(utils.format("&aUpdated breakable generator successfully"));
            player.closeInventory();
        });

        return builder.build();
    }

    /* ============================ Shared helpers ============================ */

    private void decorateBottomRow(InventoryBuilder builder, ItemStack uiItem) {
        for (int i = 0; i < 9; i++) {
            builder.setItem(27 + i, uiItem, h -> { /* decorative */ });
        }
    }

    private void relabel(ItemStack current, String title) {
        if (current == null) return;
        final ItemMeta meta = current.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName(title);
        current.setItemMeta(meta);
    }

    /** Save first 3 rows (0..26) back into generator drops */
    private void saveDropItems(InventoryBuilder builder, Inventory inv, Object gen, Player player, boolean breakable) {
        final MessageUtils utils = MessageUtils.instance;

        if (!builder.isInRegisteredInventories(inv)) {
            player.sendMessage(utils.format("&cWrong inventory found"));
            return;
        }

        if (gen instanceof Generator g) {
            g.getDropItems().clear();
            for (int i = 0; i < 27; i++) {
                ItemStack stack = inv.getItem(i);
                if (stack != null && stack.getType() != Material.AIR) {
                    g.addItemToDrop(stack);
                    player.sendMessage(utils.format("&aAdded item " + stack.getType().name() + " to generator " + g.getId()));
                }
            }
            return;
        }

        if (gen instanceof BreakableGenerator bg) {
            bg.getDropItems().clear();
            for (int i = 0; i < 27; i++) {
                ItemStack stack = inv.getItem(i);
                if (stack != null && stack.getType() != Material.AIR) {
                    bg.addItemToDrop(stack);
                    player.sendMessage(utils.format("&aAdded item " + stack.getType().name() + " to breakable generator " + bg.getId()));
                }
            }
        }
    }

}
