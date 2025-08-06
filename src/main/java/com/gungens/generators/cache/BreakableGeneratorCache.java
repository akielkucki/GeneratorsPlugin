package com.gungens.generators.cache;

import com.gungens.generators.models.BreakableGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class BreakableGeneratorCache {
    public static final BreakableGeneratorCache instance = new BreakableGeneratorCache();

    /**
     * Maps breakable generator IDs to their instances
     */
    private final Map<String, BreakableGenerator> breakableGenerators = new HashMap<>();
    private final Set<String> dirtyGenerators = new HashSet<>();
    private final Set<String> removedGenerators = new HashSet<>();

    /**
     * Maps locations to breakable generator IDs for quick location-based lookups
     */
    private final Map<Location, String> locations = new HashMap<>();

    /**
     * Set of breakable generator IDs that have been modified and need to be saved
     */
    private final Set<String> dirtyBreakableGenerators = new HashSet<>();

    /**
     * Set of breakable generator IDs that have been removed and need to be deleted from database
     */
    private final Set<String> removedBreakableGenerators = new HashSet<>();

    /**
     * Gets a breakable generator by its ID
     */
    public BreakableGenerator getBreakableGeneratorById(String id) {
        return breakableGenerators.get(id);
    }

    /**
     * Adds a breakable generator to the cache
     */
    public void addBreakableGenerator(BreakableGenerator generator, boolean loadOperation) {
        if (generator.getLocation() == null) {
            Bukkit.getLogger().warning("No location provided when adding a breakable generator!");
            return;
        }

        Location location = generator.getLocation();
        if (generator.getBlockType() == null) {
            Block block = location.getBlock();
            generator.setBlockType(block.getType());
        }

        breakableGenerators.put(generator.getId(), generator);
        locations.put(generator.getLocation(), generator.getId());

        if (!loadOperation) {
            dirtyBreakableGenerators.add(generator.getId());
        }
    }

    /**
     * Updates an existing breakable generator in the cache
     */
    public void updateBreakableGenerator(BreakableGenerator generator) {
        breakableGenerators.put(generator.getId(), generator);
        locations.put(generator.getLocation(), generator.getId());
        dirtyBreakableGenerators.add(generator.getId());
    }

    /**
     * Removes a breakable generator from the cache
     */
    public void removeBreakableGenerator(BreakableGenerator generator) {
        breakableGenerators.remove(generator.getId());
        locations.remove(generator.getLocation());
        dirtyBreakableGenerators.remove(generator.getId());
        removedBreakableGenerators.add(generator.getId());
    }

    /**
     * Checks if a location contains a breakable generator
     */
    public boolean containsLocation(Location location) {
        return locations.containsKey(location);
    }

    /**
     * Checks if a breakable generator with the given ID exists
     */
    public boolean containsBreakableGenerator(String id) {
        return breakableGenerators.containsKey(id);
    }

    /**
     * Gets a breakable generator at the specified location
     */
    public BreakableGenerator getBreakableGeneratorFromLocation(Location location) {
        String id = locations.get(location);
        return breakableGenerators.get(id);
    }

    /**
     * Gets all breakable generators as a set
     */
    public Set<BreakableGenerator> getBreakableGenerators() {
        return new HashSet<>(breakableGenerators.values());
    }

    /**
     * Gets all breakable generator IDs that need to be saved to database
     */
    public Set<String> getDirtyBreakableGenerators() {
        return dirtyBreakableGenerators;
    }

    /**
     * Gets all breakable generator IDs that need to be removed from database
     */
    public Set<String> getRemovedBreakableGenerators() {
        return removedBreakableGenerators;
    }

    /**
     * Adds multiple breakable generators to the cache during loading
     */
    public void addAll(List<BreakableGenerator> generators) {
        for (BreakableGenerator generator : generators) {
            addBreakableGenerator(generator, true);
        }

        Bukkit.getLogger().info("Added " + generators.size() + " breakable generators to the cache");
    }

    /**
     * Clears dirty and removed sets after successful database operations
     */
    public void clearDirtyAndRemoved() {
        dirtyBreakableGenerators.clear();
        removedBreakableGenerators.clear();
    }

    /**
     * Gets the total count of breakable generators
     */
    public int getBreakableGeneratorCount() {
        return breakableGenerators.size();
    }

    /**
     * Gets breakable generators owned by a specific player
     */
    public Set<BreakableGenerator> getBreakableGeneratorsByOwner(String ownerUUID) {
        Set<BreakableGenerator> ownedGenerators = new HashSet<>();
        for (BreakableGenerator generator : breakableGenerators.values()) {
            if (generator.getOwnerUUID() != null && generator.getOwnerUUID().equals(ownerUUID)) {
                ownedGenerators.add(generator);
            }
        }
        return ownedGenerators;
    }

    /**
     * Gets breakable generators by block type
     */
    public Set<BreakableGenerator> getBreakableGeneratorsByBlockType(Material blockType) {
        Set<BreakableGenerator> generatorsByType = new HashSet<>();
        for (BreakableGenerator generator : breakableGenerators.values()) {
            if (generator.getBlockType() == blockType) {
                generatorsByType.add(generator);
            }
        }
        return generatorsByType;
    }


    public Set<String> getRemovedGenerators() {
        return removedGenerators;
    }

    public BreakableGenerator getGeneratorById(String id) {
        return breakableGenerators.get(id);
    }
}