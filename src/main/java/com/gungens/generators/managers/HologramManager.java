package com.gungens.generators.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages hologram-related data for generators
 */
public class HologramManager {
    public static final HologramManager instance = new HologramManager();

    // Maps to track hologram entity UUIDs by generator ID
    private final Map<String, UUID> nameHolograms = new HashMap<>();
    private final Map<String, UUID> progressHolograms = new HashMap<>();

    // Maps to track hologram settings
    private final Map<String, Boolean> generatorHasHologram = new HashMap<>();
    private final Map<String, Boolean> generatorHasProgressBar = new HashMap<>();

    // Square characters for progress bar
    public static final String PROGRESS_FULL = "■";
    public static final String PROGRESS_EMPTY = "□";
    public static final int PROGRESS_BAR_LENGTH = 10;

    /**
     * Sets whether a generator should display a hologram.
     *
     * @param generatorId The ID of the generator
     * @param enabled True to enable the hologram, false to disable
     */
    public void setGeneratorHologram(String generatorId, boolean enabled) {
        generatorHasHologram.put(generatorId, enabled);
    }

    /**
     * Gets whether a generator has a hologram enabled.
     *
     * @param generatorId The ID of the generator
     * @return True if the hologram is enabled, false otherwise
     */
    public boolean hasGeneratorHologram(String generatorId) {
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
        generatorHasProgressBar.put(generatorId, enabled);
    }

    /**
     * Gets whether a generator has a progress bar enabled.
     *
     * @param generatorId The ID of the generator
     * @return True if the progress bar is enabled, false otherwise
     */
    public boolean hasGeneratorProgressBar(String generatorId) {
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

    /**
     * Stores the UUID of a name hologram for a generator
     */
    public void setNameHologram(String generatorId, UUID hologramUuid) {
        nameHolograms.put(generatorId, hologramUuid);
    }

    /**
     * Gets the UUID of a name hologram for a generator
     */
    public UUID getNameHologram(String generatorId) {
        return nameHolograms.get(generatorId);
    }

    /**
     * Checks if a generator has a name hologram entity
     */
    public boolean hasNameHologramEntity(String generatorId) {
        return nameHolograms.containsKey(generatorId);
    }

    /**
     * Removes a name hologram from tracking
     */
    public void removeNameHologram(String generatorId) {
        nameHolograms.remove(generatorId);
    }

    /**
     * Stores the UUID of a progress hologram for a generator
     */
    public void setProgressHologram(String generatorId, UUID hologramUuid) {
        progressHolograms.put(generatorId, hologramUuid);
    }

    /**
     * Gets the UUID of a progress hologram for a generator
     */
    public UUID getProgressHologram(String generatorId) {
        return progressHolograms.get(generatorId);
    }

    /**
     * Checks if a generator has a progress hologram entity
     */
    public boolean hasProgressHologramEntity(String generatorId) {
        return progressHolograms.containsKey(generatorId);
    }

    /**
     * Removes a progress hologram from tracking
     */
    public void removeProgressHologram(String generatorId) {
        progressHolograms.remove(generatorId);
    }

    /**
     * Clear all hologram entries
     */
    public void clearAll() {
        nameHolograms.clear();
        progressHolograms.clear();
    }
}
