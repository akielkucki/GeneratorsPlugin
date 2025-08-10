package com.gungens.generators.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BreakableGeneratorUIItems {

    public final ItemStack SET_HEALTH;
    public final ItemStack CANCEL;
    public final ItemStack OK;
    public final ItemStack CLOSE_WITHOUT_SAVING;
    public final ItemStack SAVE_AND_CLOSE;
    public final ItemStack UI_ITEM;
    public ItemStack SET_GLOWING;
    public ItemStack TOGGLE_NAME;
    public ItemStack SET_RESET_TIME;

    {
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
    }

    public BreakableGeneratorUIItems(BreakableGenerator generator) {
        SET_GLOWING = new ItemStack(Material.NETHER_STAR);
        ItemMeta setGlowingItemMeta = SET_GLOWING.getItemMeta();
        setGlowingItemMeta.setDisplayName("§aGLOWING: " + generator.isGlowing());
        SET_GLOWING.setItemMeta(setGlowingItemMeta);

        TOGGLE_NAME = new ItemStack(Material.OAK_SIGN);
        ItemMeta toggleNameItemMeta = TOGGLE_NAME.getItemMeta();
        toggleNameItemMeta.setDisplayName("§aSHOW NAME: " + generator.isNameVisible());
        TOGGLE_NAME.setItemMeta(toggleNameItemMeta);

        SET_RESET_TIME = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta setResetTimeItemMeta = SET_RESET_TIME.getItemMeta();
        setResetTimeItemMeta.setDisplayName("§dRESET TIME: " + generator.getResetTime() + "s");
        SET_RESET_TIME.setItemMeta(setResetTimeItemMeta);

        SET_HEALTH = new ItemStack(Material.REDSTONE);
        ItemMeta setHealthMeta = SET_HEALTH.getItemMeta();
        setHealthMeta.setDisplayName("§cSET HEALTH: " + generator.getMaxHealth() + "hp");
        SET_HEALTH.setItemMeta(setHealthMeta);

    }
}