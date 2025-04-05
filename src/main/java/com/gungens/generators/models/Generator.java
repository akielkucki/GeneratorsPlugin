package com.gungens.generators.models;

import com.gungens.generators.Generators;
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
import java.util.*;
import java.util.logging.Level;

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
    private double tickTime;

    @DatabaseField(columnName = "multiplier")
    private double multiplier;

    @DatabaseField(columnName = "is_admin")
    private boolean isAdminGenerator;

    @DatabaseField(columnName = "owner_uuid")
    private String ownerUUID;
    private Material blockType;

    private int lastDropIndex = -1;

    @DatabaseField(columnName = "is_glowing", dataType = DataType.BOOLEAN)
    private boolean isGlowing;
    @DatabaseField(columnName = "is_name_visible", dataType = DataType.BOOLEAN)
    private boolean isNameVisible;

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
            dropItems = deserializeItemStacks(dropItemsSerialized);
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
        this.dropItemsSerialized = serializeItemStacks(dropItems);
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

    public static String serializeItemStacks(List<ItemStack> items) {
        StringBuilder serialized = new StringBuilder();
        for (ItemStack itemStack : items) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeObject(itemStack);
                dataOutput.close();
                serialized.append(Base64.getEncoder().encodeToString(outputStream.toByteArray())).append(';');
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize ItemStack", e);
            }
        }
        System.out.println(serialized.toString());
        return serialized.toString();
    }

    public static List<ItemStack> deserializeItemStacks(String base64arr) {
        String[] parts = base64arr.split(";");
        List<ItemStack> items = new ArrayList<>();

        for (String part : parts) {
            if (part.isEmpty()) continue;
            try {
                byte[] data = Base64.getDecoder().decode(part);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                ItemStack item = (ItemStack) dataInput.readObject();
                dataInput.close();
                items.add(item);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize ItemStack", e);
            }
        }

        return items;
    }
    /**
     *   continue refactoring classes to work with array of item stacks
     *   Gui to add/edit multiple items to the list
     *   Ensure items are serialized properly
     *   Tick intervals for generators are fixed
     *   Each generator has a hologram above it
     *   Each generator has a loading bar above it
     */

}
