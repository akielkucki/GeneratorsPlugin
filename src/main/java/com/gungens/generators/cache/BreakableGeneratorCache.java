package com.gungens.generators.cache;

import com.gungens.generators.Generators;
import com.gungens.generators.db.DbManager;
import com.gungens.generators.libs.InventoryBuilder;
import com.gungens.generators.models.BreakableGenerator;
import com.gungens.generators.models.CommandState;
import com.gungens.generators.models.Generator;
import com.gungens.generators.models.PlayerQueueTrack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class BreakableGeneratorCache {
    public static final BreakableGeneratorCache instance = new BreakableGeneratorCache();
    private static final Logger log = LoggerFactory.getLogger(BreakableGeneratorCache.class);

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
    private final Map<String, PlayerQueueTrack> playerChatCommandQueue = new HashMap<>();
    private final Map<String, InventoryBuilder> currentInventoryTrack = new HashMap<>();
    private final Map<String, String> currentGeneratorTrack = new HashMap<>();

    /**
     * Adds a breakable generator to the cache
     */
    public void addBreakableGenerator(BreakableGenerator generator, boolean loadOperation) {
        if (generator.getLocation() == null) {
            Bukkit.getLogger().warning("No location provided when adding a breakable generator!");
            return;
        }
        generator.setDropItems(generator.getDropItems());
        generator.getLocation().getBlock().setType(Material.valueOf(generator.getBlockTypeName()));
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
        try {
            Generators.instance.getDbManager().flushDirtyGenerators();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
    public void addPlayerToQueue(String uuid, InventoryBuilder builder, String generatorId, String commandName) {
        playerChatCommandQueue.put(uuid, new PlayerQueueTrack(commandName));
        currentInventoryTrack.put(uuid,builder);
        currentGeneratorTrack.put(uuid,generatorId);
    }
    public PlayerQueueTrack getPlayerCommandQueueState(String uuid) {
        return playerChatCommandQueue.get(uuid);
    }
    public void removePlayerFromQueue(String uuid) {
        playerChatCommandQueue.remove(uuid);
        currentInventoryTrack.remove(uuid);
        currentGeneratorTrack.remove(uuid);
    }
    public void setPlayerCommandQueueState(String uuid, CommandState state, double interval) {
        PlayerQueueTrack track = playerChatCommandQueue.get(uuid);
        track.setState(state, interval);
    }
    public InventoryBuilder getCurrentInventoryTrack(String uuid) {
        return currentInventoryTrack.get(uuid);
    }
    public String getCurrentGeneratorTrack(String uuid) {
        return currentGeneratorTrack.get(uuid);
    }

    public boolean isInCommandQueue(String uuid) {
        return playerChatCommandQueue.containsKey(uuid);
    }

    public void updateGenerator(BreakableGenerator breakableGenerator) {
        breakableGenerators.put(breakableGenerator.getId(), breakableGenerator);
        locations.put(breakableGenerator.getLocation(), breakableGenerator.getId());
        dirtyBreakableGenerators.add(breakableGenerator.getId());
    }
}