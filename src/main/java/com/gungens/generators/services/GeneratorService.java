package com.gungens.generators.services;

import com.gungens.generators.Generators;
import com.gungens.generators.libs.MessageUtils;
import com.gungens.generators.managers.HologramManager;
import com.gungens.generators.models.BreakableGenerator;
import com.gungens.generators.models.Generator;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.logging.Level;

public class GeneratorService {
    private static final GeneratorService instance = new GeneratorService();

    public static GeneratorService getInstance() {

        return instance;

    }

    public void spawnItem(Generator generator) {
        Location location = generator.getLocation();
        ItemStack dropItem = generator.getNextItemToDrop();
        if (location == null) {
            return;
        }
        if (dropItem == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Item at generator " + generator.getId() + " is null!");
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
                    MessageUtils.instance.capitalizeFirstLetter(item.getItemStack().getType().name().toLowerCase())
            );
        }
        item.setGlowing(generator.isGlowing());
        item.setCustomNameVisible(generator.isNameVisible());
        generator.updateLastDropTime();
    }
    public void spawnItem(BreakableGenerator breakableGenerator) {
        Location location = breakableGenerator.getLocation();
        ItemStack dropItem = breakableGenerator.getDropItems().get(breakableGenerator.getIndex());
        if (location == null) {
            return;
        }
        if (dropItem == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Item at generator " + breakableGenerator.getId() + " is null!");
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
                    MessageUtils.instance.capitalizeFirstLetter(item.getItemStack().getType().name().toLowerCase())
            );
        }
        item.setGlowing(breakableGenerator.isGlowing());
        item.setCustomNameVisible(breakableGenerator.isNameVisible());
        breakableGenerator.setIndex();
    }

    public void spawnHologramIfNotPresent(Generator generator) {
        if (!HologramManager.instance.hasGeneratorHologram(generator.getId())) {
            if (HologramManager.instance.hasNameHologramEntity(generator.getId())) {
                removeHologram(generator.getId(), true, false);
            }
            return;
        }

        Location location = generator.getLocation();
        if (location == null) {
            return;
        }

        Location hologramLoc = location.clone().add(0.5, 2.0, 0.5);

        if (HologramManager.instance.hasNameHologramEntity(generator.getId())) {
            ArmorStand nameStand = getArmorStand(HologramManager.instance.getNameHologram(generator.getId()));
            if (nameStand != null) {
                nameStand.setCustomName(MessageUtils.instance.format(generator.getHologramName()));
                return;
            }
            HologramManager.instance.removeNameHologram(generator.getId());
        }

        ArmorStand nameStand = (ArmorStand) location.getWorld().spawnEntity(hologramLoc, EntityType.ARMOR_STAND);
        setupHologramEntity(nameStand);
        nameStand.setCustomName(MessageUtils.instance.format(generator.getHologramName()));
        HologramManager.instance.setNameHologram(generator.getId(), nameStand.getUniqueId());
    }

    public void spawnProgressBarIfNotPresent(Generator generator) {
        if (!HologramManager.instance.hasGeneratorProgressBar(generator.getId())) {
            if (HologramManager.instance.hasProgressHologramEntity(generator.getId())) {
                removeHologram(generator.getId(), false, true);
            }
            return;
        }

        Location location = generator.getLocation();
        if (location == null) {
            return;
        }

        Location progressLoc = location.clone().add(0.5, 1.7, 0.5);
        String progressBar = createProgressBar(generator);

        if (HologramManager.instance.hasProgressHologramEntity(generator.getId())) {
            ArmorStand progressStand = getArmorStand(HologramManager.instance.getProgressHologram(generator.getId()));
            if (progressStand != null) {

                progressStand.setCustomName(progressBar);
                return;
            }
            HologramManager.instance.removeProgressHologram(generator.getId());
        }

        ArmorStand progressStand = (ArmorStand) location.getWorld().spawnEntity(progressLoc, EntityType.ARMOR_STAND);
        setupHologramEntity(progressStand);
        progressStand.setCustomName(progressBar);
        HologramManager.instance.setProgressHologram(generator.getId(), progressStand.getUniqueId());
    }

    private String createProgressBar(Generator generator) {
        double progressPercent = generator.getProgressPercentage();
        int filledBars = (int) Math.floor(progressPercent * HologramManager.PROGRESS_BAR_LENGTH);

        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < HologramManager.PROGRESS_BAR_LENGTH; i++) {
            if (i-1 < filledBars) {
                progressBar.append(HologramManager.PROGRESS_FULL);
            } else {
                progressBar.append(HologramManager.PROGRESS_EMPTY);
            }
        }

        return progressBar.toString();
    }

    private void setupHologramEntity(ArmorStand stand) {
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(true);
        stand.setMarker(true);
        stand.getPersistentDataContainer().set(new NamespacedKey(Generators.instance, "hologram"), PersistentDataType.STRING, UUID.randomUUID().toString());
    }

    private ArmorStand getArmorStand(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uuid) && entity instanceof ArmorStand) {
                    return (ArmorStand) entity;
                }
            }
        }
        return null;
    }

    public void removeHologram(String generatorId, boolean removeName, boolean removeProgress) {
        if (removeName && HologramManager.instance.hasNameHologramEntity(generatorId)) {
            UUID uuid = HologramManager.instance.getNameHologram(generatorId);
            ArmorStand stand = getArmorStand(uuid);
            if (stand != null) {
                stand.remove();
            }
            HologramManager.instance.removeNameHologram(generatorId);
        }

        if (removeProgress && HologramManager.instance.hasProgressHologramEntity(generatorId)) {
            UUID uuid = HologramManager.instance.getProgressHologram(generatorId);
            ArmorStand stand = getArmorStand(uuid);
            if (stand != null) {
                stand.remove();
            }
            HologramManager.instance.removeProgressHologram(generatorId);
        }
    }

    public void clearExistingArmorStands() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(ArmorStand.class)) {
                if (entity instanceof ArmorStand) {
                    ArmorStand stand = (ArmorStand) entity;
                    if (!stand.isVisible() && !stand.hasGravity() && stand.isCustomNameVisible() && stand.getPersistentDataContainer().has(new NamespacedKey(Generators.instance, "hologram"), PersistentDataType.STRING)) {
                        stand.remove();
                    }
                }
            }
        }

        HologramManager.instance.clearAll();
        Bukkit.getLogger().info("Cleared existing armor stands used for holograms");
    }
}
