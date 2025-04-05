package com.gungens.generators.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class GeneratorUIItems {

    public static final ItemStack CANCEL;
    public static final ItemStack OK;
    public static final ItemStack CLOSE_WITHOUT_SAVING;
    public static final ItemStack SAVE_AND_CLOSE;
    public static final ItemStack UI_ITEM;
    public static final ItemStack SET_GLOWING;
    public static final ItemStack TOGGLE_NAME;
    public static final ItemStack SET_INTERVAL;

    static {
        UI_ITEM = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta uiMeta = UI_ITEM.getItemMeta();
        uiMeta.setDisplayName("§0");
        UI_ITEM.setItemMeta(uiMeta);

        CANCEL = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = CANCEL.getItemMeta();
        cancelMeta.setDisplayName("§cCancel");
        CANCEL.setItemMeta(cancelMeta);

        OK = new ItemStack(Material.GREEN_WOOL);
        ItemMeta okMeta = OK.getItemMeta();
        okMeta.setDisplayName("§aConfirm");
        OK.setItemMeta(okMeta);

        CLOSE_WITHOUT_SAVING = new ItemStack(Material.RED_WOOL);
        ItemMeta closeMeta = CLOSE_WITHOUT_SAVING.getItemMeta();
        closeMeta.setDisplayName("§cClose without saving");
        CLOSE_WITHOUT_SAVING.setItemMeta(closeMeta);

        SAVE_AND_CLOSE = new ItemStack(Material.GREEN_WOOL);
        ItemMeta saveAndCloseMeta = SAVE_AND_CLOSE.getItemMeta();
        saveAndCloseMeta.setDisplayName("§aSave items and close");
        SAVE_AND_CLOSE.setItemMeta(saveAndCloseMeta);

        SET_GLOWING = new ItemStack(Material.NETHER_STAR);
        ItemMeta setGlowingItemMeta = SET_GLOWING.getItemMeta();
        setGlowingItemMeta.setDisplayName("§aGLOWING: false");
        SET_GLOWING.setItemMeta(setGlowingItemMeta);

        TOGGLE_NAME = new ItemStack(Material.OAK_SIGN);
        ItemMeta toggleNameItemMeta = TOGGLE_NAME.getItemMeta();
        toggleNameItemMeta.setDisplayName("§aSHOW NAME: false");
        TOGGLE_NAME.setItemMeta(toggleNameItemMeta);

        SET_INTERVAL = new ItemStack(Material.CLOCK);
        ItemMeta setIntervalItemMeta = SET_INTERVAL.getItemMeta();
        setIntervalItemMeta.setDisplayName("§dSET INTERVAL: 1");
        SET_INTERVAL.setItemMeta(setIntervalItemMeta);
    }

    private GeneratorUIItems() {
        // prevent instantiation
    }
}

