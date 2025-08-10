
package com.gungens.generators.models;

import com.gungens.generators.libs.GeneratorUtils;
import com.gungens.generators.services.GeneratorService;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DatabaseTable(tableName = "breakable_generators")
public class BreakableGenerator {

    // Primary key stored as UUID
    @DatabaseField(id = true, columnName = "id", dataType = DataType.STRING)
    private String id;

    // Store the location as a serialized string
    @DatabaseField(columnName = "location")
    private String locationSerialized;
    // transient field for runtime use
    private transient Location location;

    @DatabaseField(columnName = "multiplier")
    private double multiplier = 1.0;

    private transient boolean isAdminGenerator;
    @DatabaseField(columnName = "drop_items")
    private String dropItemsSerialized;

    private transient List<ItemStack> dropItems = new ArrayList<>();
    @DatabaseField(columnName = "owner_uuid")
    private String ownerUUID;
    @DatabaseField(columnName = "block_type", dataType = DataType.STRING)
    private String blockTypeName;

    private transient Material blockType;

    @DatabaseField(columnName = "glowing", dataType = DataType.BOOLEAN)
    private boolean isGlowing;
    @DatabaseField(columnName = "nameVisible", dataType = DataType.BOOLEAN)
    private boolean nameVisible;
    @DatabaseField(columnName = "reset_time")
    private double resetTime = 5.0;
    @DatabaseField(columnName = "max_health")
    private double maxHealth = 0.0;

    private transient double health = 0;


    // Progress tracking for breakable generators
    private transient long lastActionTime;
    private transient int index;

    // Default constructor for ORM
    public BreakableGenerator() {}

    // Constructor with block type
    public BreakableGenerator(Material blockType) {
        this.id = UUID.randomUUID().toString();
        this.multiplier = 1.0;
        this.isAdminGenerator = false;
        this.ownerUUID = "";
        this.blockType = blockType;
        this.isGlowing = false;
        this.dropItems = new ArrayList<>();
        this.maxHealth = 0.0;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex() {
        this.index = (index + 1) % dropItems.size();
    }
    public void updateLastActionTime() {
        this.lastActionTime = System.currentTimeMillis();
    }

    public long getLastActionTime() {
        return lastActionTime;
    }

    public void setLastActionTime(long lastActionTime) {
        this.lastActionTime = lastActionTime;
    }
    public void addItemToDrop(ItemStack item) {
        dropItems.add(item);
    }
    public List<ItemStack> getDropItems() {
        if (dropItems.isEmpty() && dropItemsSerialized != null) {
            dropItems = GeneratorUtils.instance.deserializeItemStacks(dropItemsSerialized);
        }
        return dropItems;
    }
    // Basic getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isGlowing() {
        return isGlowing;
    }

    public void setGlowing(boolean glowing) {
        isGlowing = glowing;
    }
    public boolean isNameVisible() {
        return nameVisible;
    }
    public void setNameVisible(boolean nameVisible) {
        this.nameVisible = nameVisible;
    }
    public double getResetTime() {
        return resetTime;
    }

    public void setResetTime(double resetTime) {
        this.resetTime = resetTime;
    }

    // Location serialization methods
    public static String serializeLocation(Location location) {
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

    @Override
    public String toString() {
        return "BreakableGenerator{" +
                "id='" + id + '\'' +
                ", location=" + (location != null ? location.toString() : "null") +
                ", multiplier=" + multiplier +
                ", ownerUUID='" + ownerUUID + '\'' +
                ", blockType=" + blockType +
                ", isGlowing=" + isGlowing +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BreakableGenerator that = (BreakableGenerator) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public void setDropItems(List<ItemStack> dropItems) {
        this.dropItems = dropItems;
        this.dropItemsSerialized = GeneratorUtils.serializeItemStacks(dropItems);
    }

    public double getHealth() {
        return this.health;
    }
    public void setHealth(double health) {
        this.health = health;
    }
    public double getMaxHealth() {
        return this.maxHealth;
    }
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }
    public void damage(int damage) {
        this.health -= damage;
        GeneratorService.getInstance().spawnHealthBarIfNotPresent(this);

    }

    public void setBlockTypeName(String name) {
        this.blockTypeName = name;
    }
    public String getBlockTypeName() {
        return this.blockTypeName;
    }
}