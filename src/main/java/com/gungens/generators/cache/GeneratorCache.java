package com.gungens.generators.cache;

import com.gungens.generators.libs.InventoryBuilder;
import com.gungens.generators.models.CommandState;
import com.gungens.generators.models.Generator;
import com.gungens.generators.models.PlayerQueueTrack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GeneratorCache {
    public static final GeneratorCache instance = new GeneratorCache();
    private static final Logger log = LoggerFactory.getLogger(GeneratorCache.class);
    /**
     */
    private final Map<String, Generator> generators = new HashMap<>();

    /**
     */
    private final Map<Location, String> locations = new HashMap<>();

    /**
     */
    private final Set<String> dirtyGenerators = new HashSet<>();

    /**
     */
    private final Set<String> removedGenerators = new HashSet<>();

    /**
     */
    private final Map<String, PlayerQueueTrack> playerChatCommandQueue = new HashMap<>();

    /**
     */
    private final Map<String, InventoryBuilder> currentInventoryTrack = new HashMap<>();

    /**
     */
    private final Map<String, String> currentGeneratorTrack = new HashMap<>();

    /**
     */
    private final Map<String, Boolean> generatorHasHologram = new HashMap<>(); //Make methods needed for this

    /**
     */
    private final Map<String, Boolean> generatorHasProgressBar = new HashMap<>(); //Make methods needed for this


    public Generator getGeneratorById(String id) {
        return generators.get(id);
    }
    public void addGenerator(Generator generator, boolean loadOperation) {
        if (generator.getLocation() == null) {
            Bukkit.getLogger().warning("No location provided when adding a generator!");
            return;
        }
        generator.getLocation().getBlock().setType(Material.valueOf(generator.getBlockTypeName()));

        generators.put(generator.getId(), generator);
        locations.put(generator.getLocation(), generator.getId());
        if (!loadOperation) {
            dirtyGenerators.add(generator.getId());
        }
    }
    public void updateGenerator(Generator generator) {
        generators.put(generator.getId(), generator);
        locations.put(generator.getLocation(), generator.getId());
        dirtyGenerators.add(generator.getId());
    }
    public void removeGenerator(Generator generator) {
        generators.remove(generator.getId());
        locations.remove(generator.getLocation());
        dirtyGenerators.remove(generator.getId());
        removedGenerators.add(generator.getId());
    }
    public boolean containsLocation(Location location) {
        return locations.containsKey(location);
    }
    public boolean containsGenerator(String id) {
        return generators.containsKey(id);
    }
    public Generator getGeneratorFromLocation(Location location) {
        String id = locations.get(location);
        return generators.get(id);
    }

    public Set<Generator> getGenerators() {
        return new HashSet<>(generators.values());
    }
    public Set<String> getDirtyGenerators() {
        return dirtyGenerators;
    }
    public Set<String> getRemovedGenerators() {
        return removedGenerators;
    }

    public void addAll(List<Generator> generators) {
        for (Generator generator : generators) {
            generator.setBlockType( Material.valueOf( generator.getBlockTypeName() ) );
            generator.getLocation().getBlock().setType(generator.getBlockType());
            log.info("Adding generator {} {}", generator.getLocation().toString(), generator.getBlockType());
            addGenerator(generator, true);
        }

       Bukkit.getLogger().info("Added " + generators.size() + " generators to the cache");
    }

    public void addPlayerToQueue(String uuid, InventoryBuilder builder, String generatorId, String commandName) {
        playerChatCommandQueue.put(uuid, new PlayerQueueTrack(commandName));
        currentInventoryTrack.put(uuid,builder);
        currentGeneratorTrack.put(uuid,generatorId);
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
    public boolean isInCommandQueue(String uuid) {
        return playerChatCommandQueue.containsKey(uuid);
    }
    public InventoryBuilder getCurrentInventoryTrack(String uuid) {
        return currentInventoryTrack.get(uuid);
    }
    public String getCurrentGeneratorTrack(String uuid) {
        return currentGeneratorTrack.get(uuid);
    }

    /**
     * Sets whether a generator should display a hologram.
     *
     * @param generatorId The ID of the generator
     * @param enabled True to enable the hologram, false to disable
     */
    public void setGeneratorHologram(String generatorId, boolean enabled) {
        if (!generators.containsKey(generatorId)) {
            return;
        }

        generatorHasHologram.put(generatorId, enabled);
        dirtyGenerators.add(generatorId);
    }

    /**
     * Gets whether a generator has a hologram enabled.
     *
     * @param generatorId The ID of the generator
     * @return True if the hologram is enabled, false otherwise
     */
    public boolean hasGeneratorHologram(String generatorId) {
        if (!generators.containsKey(generatorId)) {
            return false;
        }

        Boolean hasHologram = generatorHasHologram.get(generatorId);
        return hasHologram != null && hasHologram;
    }

    /**
     * Sets whether a generator should display a progress bar.
     *
     * @param generatorId The ID of the generator
     * @param enabled True to enable the progress bar, false to disable
     */
    public void setGeneratorProgressBar(String generatorId, boolean enabled) {
        if (!generators.containsKey(generatorId)) {
            return;
        }

        generatorHasProgressBar.put(generatorId, enabled);
        dirtyGenerators.add(generatorId);
    }

    /**
     * Gets whether a generator has a progress bar enabled.
     *
     * @param generatorId The ID of the generator
     * @return True if the progress bar is enabled, false otherwise
     */
    public boolean hasGeneratorProgressBar(String generatorId) {
        if (!generators.containsKey(generatorId)) {
            return false;
        }

        Boolean hasProgressBar = generatorHasProgressBar.get(generatorId);
        return hasProgressBar != null && hasProgressBar;
    }

    /**
     * Enables or disables both hologram and progress bar for a generator.
     *
     * @param generatorId The ID of the generator
     * @param enabled True to enable both, false to disable both
     */
    public void setGeneratorVisuals(String generatorId, boolean enabled) {
        setGeneratorHologram(generatorId, enabled);
        setGeneratorProgressBar(generatorId, enabled);
    }

    public void clearDirtyAndRemoved() {
        dirtyGenerators.clear();
        removedGenerators.clear();
    }
}
