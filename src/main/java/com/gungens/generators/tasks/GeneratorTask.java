package com.gungens.generators.tasks;

import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.models.Generator;
import com.gungens.generators.services.GeneratorService;

import java.util.HashMap;
import java.util.Map;

public class GeneratorTask implements Runnable {

    private final Map<String, Float> generatorTickCounters = new HashMap<>();

    @Override
    public void run() {
        for (String removedId : GeneratorCache.instance.getRemovedGenerators()) {
            generatorTickCounters.remove(removedId);
        }
        for (Generator generator : GeneratorCache.instance.getGenerators()) {
            if (!generator.getLocation().getChunk().getPlayersSeeingChunk().isEmpty()) {
                // Skip generators in unloaded chunks
                if (!generator.getLocation().getChunk().isLoaded()) {
                    continue;
                }

                String generatorId = generator.getId();
                float currentTicks = generatorTickCounters.getOrDefault(generatorId, 0.0F);
                double tickTime = generator.getTickTime();

                if (tickTime <= 0) {
                    continue;
                }

                currentTicks += 0.1F;

                if (currentTicks >= tickTime) {
                    GeneratorService.getInstance().spawnItem(generator);
                    currentTicks = 0;
                }

                generator.updateProgress(currentTicks);
                generatorTickCounters.put(generatorId, currentTicks);


                GeneratorService.getInstance().spawnHologramIfNotPresent(generator);
                GeneratorService.getInstance().spawnProgressBarIfNotPresent(generator);


            }
        }
    }
}