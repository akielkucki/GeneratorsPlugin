package com.gungens.generators.tasks;

import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.models.Generator;
import com.gungens.generators.services.GeneratorService;

import java.util.HashMap;
import java.util.Map;

public class GeneratorTask implements Runnable {

    private final Map<String, Double> generatorTickCounters = new HashMap<>();

    @Override
    public void run() {
        for (Generator generator : GeneratorCache.instance.getGenerators()) {

            String generatorId = generator.getId();
            double currentTicks = generatorTickCounters.getOrDefault(generatorId, 0.0d);
            double tickTime = generator.getTickTime();

            if (tickTime <= 0) {
                continue;
            }

            currentTicks+= 0.1;

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