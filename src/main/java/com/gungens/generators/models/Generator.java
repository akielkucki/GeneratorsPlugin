package com.gungens.generators.models;

import com.gungens.generators.libs.GeneratorUtils;
import com.gungens.generators.libs.MessageUtils;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@DatabaseTable(tableName = "generators")
public class Generator {

    // Primary key stored as UUID
    @DatabaseField(id = true, columnName = "id", dataType = DataType.STRING)
    private String id;

    // Store the location as a serialized string.
    @DatabaseField(columnName = "location")
    private String locationSerialized;
    // transient field for runtime use
    private transient Location location;

    // Store the drop item as a serialized Base64 string.
    @DatabaseField(columnName = "drop_items")
    private String dropItemsSerialized;

    private transient List<ItemStack> dropItems;

    @DatabaseField(columnName = "tick_time")
    private double tickTime = 1.0;

    @DatabaseField(columnName = "multiplier")
    private double multiplier;

    private transient boolean isAdminGenerator;

    @DatabaseField(columnName = "owner_uuid")
    private String ownerUUID;
    @DatabaseField(columnName = "block_type", dataType = DataType.STRING)
    private String blockTypeName;

    private transient Material blockType;

    private int lastDropIndex = -1;

    @DatabaseField(columnName = "glowing", dataType = DataType.BOOLEAN)
    private boolean isGlowing;
    @DatabaseField(columnName = "name_visible", dataType = DataType.BOOLEAN)
    private boolean isNameVisible;
    @DatabaseField(columnName = "holo_visible", dataType = DataType.BOOLEAN)
    private boolean isHologramVisible;

    private transient long lastDropTime;
    private transient double currentProgress = 0.0;

    public void updateProgress(double currentTicks) {
        if (tickTime <= 0) {
            currentProgress = 0.0;
        } else {
            currentProgress = currentTicks / tickTime;
        }
    }

    public double getProgressPercentage() {
        return currentProgress;
    }

    public void updateLastDropTime() {
        this.lastDropTime = System.currentTimeMillis();
    }

    public String getHologramName() {
        List<ItemStack> items = getDropItems();
        if (items == null || items.isEmpty()) {
            return "Generator";
        }
        ItemStack item = items.get(0);
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName() + " Generator";
        } else {
            String itemType = item.getType().name().toLowerCase().replace("_", " ");
            return MessageUtils.instance.capitalizeFirstLetter(itemType) + " Generator";
        }
    }

    public int getDropIntervalTicks() {
        return (int) tickTime;
    }

    public void setDropIntervalTicks(int dropIntervalTicks) {
        this.tickTime = dropIntervalTicks;
    }

    public long getLastDropTime() {
        return lastDropTime;
    }

    public void setLastDropTime(long lastDropTime) {
        this.lastDropTime = lastDropTime;
    }


    public Generator() {}

    public Generator(Material blockType) {
        dropItems = new ArrayList<>();
        this.id = UUID.randomUUID().toString();
        this.tickTime = 0;
        this.multiplier = 1.0;
        this.isAdminGenerator = false;
        this.ownerUUID = "";
        this.blockType = blockType;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        if (location == null && locationSerialized != null) {
            location = deserializeLocation(locationSerialized);
        }
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        this.locationSerialized = serializeLocation(location);
    }

    public List<ItemStack> getDropItems() {
        if (dropItems == null && dropItemsSerialized != null) {
            dropItems = GeneratorUtils.instance.deserializeItemStacks(dropItemsSerialized);
        }
        return dropItems;
    }
    public ItemStack getNextItemToDrop() {
        List<ItemStack> dropItems = getDropItems();
        lastDropIndex = (lastDropIndex + 1) % dropItems.size();
        return dropItems.get(lastDropIndex);
    }

    public void setDropItems(List<ItemStack> dropItems) {
        this.dropItems = dropItems;
        this.dropItemsSerialized = GeneratorUtils.serializeItemStacks(dropItems);
    }
    public void addItemToDrop(ItemStack item) {
        dropItems.add(item);
    }

    public double getTickTime() {
        return tickTime;
    }

    public void setTickTime(double tickTime) {
        this.tickTime = tickTime;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isAdminGenerator() {
        return isAdminGenerator;
    }

    public void setAdminGenerator(boolean adminGenerator) {
        isAdminGenerator = adminGenerator;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(String ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public Material getBlockType() {
        return blockType;
    }

    public void setBlockType(Material blockType) {
        this.blockType = blockType;
    }
    public void setGlowing(boolean glowing) {
        this.isGlowing = glowing;
    }
    public boolean isGlowing() {
        return isGlowing;
    }
    public void setNameVisible(boolean nameVisible) {
        this.isNameVisible = nameVisible;
    }
    public boolean isNameVisible() {
        return isNameVisible;
    }

    public static String serializeLocation(Location location) {
        // Simple delimiter-based serialization (world;x;y;z)

        return location.getWorld().getName() + ";" +
                location.getX() + ";" +
                location.getY() + ";" +
                location.getZ();
    }

    public static Location deserializeLocation(String serialized) {
        String[] parts = serialized.split(";");
        World world = Bukkit.getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);

        return new Location(world, x, y, z);
    }

    public void setBlockTypeName(String name) {
        this.blockTypeName = name;
    }
    public String getBlockTypeName() {
        return this.blockTypeName;
    }

    public boolean isHologramVisible() {
        return isHologramVisible;
    }

    public void setHologramVisible(boolean hologramVisible) {
        isHologramVisible = hologramVisible;
    }
}
