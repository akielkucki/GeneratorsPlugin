package com.gungens.generators.cache;

import com.gungens.generators.Generators;
import com.gungens.generators.libs.InventoryBuilder;
import com.gungens.generators.models.CommandState;
import com.gungens.generators.models.Generator;
import com.gungens.generators.models.InventoryState;
import com.gungens.generators.models.PlayerQueueTrack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class GeneratorCache {
    public static GeneratorCache instance = new GeneratorCache();
    private final Map<String, Generator> generators = new HashMap<>();
    private final Map<Location, String> locations = new HashMap<>();

    private final Set<String> dirtyGenerators = new HashSet<>();
    private final Set<String> removedGenerators = new HashSet<>();
    private final HashMap<String, PlayerQueueTrack> playerChatCommandQueue = new HashMap<>();


    public Generator getGeneratorById(String id) {
        return generators.get(id);
    }
    public void addGenerator(Generator generator, boolean loadOperation) {
        if (generator.getLocation() == null) {
            Bukkit.getLogger().warning("No location provided when adding a generator!");
            return;
        }
        Location location = generator.getLocation();
        if (generator.getBlockType() == null) {
            Block block = location.getBlock();
            generator.setBlockType(block.getType());
        }
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

            addGenerator(generator, true);
        }

       Bukkit.getLogger().info("Added " + generators.size() + " generators to the cache");
    }

    public void addPlayerToQueue(String uuid) {
        playerChatCommandQueue.put(uuid, new PlayerQueueTrack());
    }
    public void removePlayerFromQueue(String uuid) {
        playerChatCommandQueue.remove(uuid);
    }
    public void setPlayerCommandQueueState(String uuid, CommandState state, double interval) {
        PlayerQueueTrack track = playerChatCommandQueue.get(uuid);
        track.setState(state, interval);
    }
    public boolean isInCommandQueue(String uuid) {
        return playerChatCommandQueue.containsKey(uuid);
    }
}
