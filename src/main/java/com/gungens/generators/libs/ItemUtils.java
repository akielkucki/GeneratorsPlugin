package com.gungens.generators.libs;

import com.gungens.generators.models.Generator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static com.gungens.generators.libs.CentralKeys.GEN_ID;

public class ItemUtils {
    public static final ItemUtils instance = new ItemUtils();
    private final MessageUtils utils = new MessageUtils();

    public ItemStack createGeneratorItem(Material material, String ownerName, Generator generator) {
        ItemStack generatorBlock = new ItemStack(material);

        ItemMeta meta = generatorBlock.getItemMeta();
        meta.setDisplayName(utils.format("&6Generator"));
        List<String> lore = new ArrayList<>();

        lore.add(utils.format("&9Place this to create a generator"));
        lore.add(utils.format("&7Owned by &6" + ownerName));
        lore.add(utils.format("&aPlugin made by GunGens"));
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(GEN_ID, PersistentDataType.STRING, generator.getId());
        generatorBlock.setItemMeta(meta);
        return generatorBlock;
    }
    public ItemStack createDropItem() {
        ItemStack dropItem = new ItemStack(Material.STONE);
        ItemMeta dropItemMeta = dropItem.getItemMeta();
        dropItemMeta.setDisplayName(utils.format("&7Example item"));
        dropItem.setItemMeta(dropItemMeta);
        return dropItem;
    }
}
