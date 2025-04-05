package com.gungens.generators.services;

import com.gungens.generators.libs.MessageUtils;
import com.gungens.generators.models.Generator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.logging.Level;

public class GeneratorService {

    public static GeneratorService getInstance() {
        return new GeneratorService();
    }
    /**
     * Spawns the drop item above the generator's location.
     */
    public void spawnItem(Generator generator) {
        Location location = generator.getLocation();
        ItemStack dropItem = generator.getNextItemToDrop();
        if (location == null) {
            return;
        }
        if (dropItem == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Item at generator "+generator.getId()+" is null!");
            return;
        }

        Location spawnLoc = location.clone().add(0.5, 1.2, 0.5);
        Item item = location.getWorld().dropItem(spawnLoc, dropItem);
        item.setVelocity(new Vector(0, 0, 0));
        if (item.getItemStack().hasItemMeta() &&
            item.getItemStack().getItemMeta().hasDisplayName()) {
            item.setCustomName(MessageUtils.instance.format(item.getItemStack().getItemMeta().getDisplayName()));

        } else {
            item.setCustomName(
                    capitalizeFirstLetter(item.getItemStack().getType().name().toLowerCase())
            );
        }
        item.setGlowing(generator.isGlowing());
        item.setCustomNameVisible(generator.isNameVisible());
    }
    public String capitalizeFirstLetter(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
