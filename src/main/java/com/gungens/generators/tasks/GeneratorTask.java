package com.gungens.generators.tasks;

import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.models.Generator;
import com.gungens.generators.services.GeneratorService;

public class GeneratorTask implements Runnable {
    @Override
    public void run() {
        for (Generator generator : GeneratorCache.instance.getGenerators()) {
            GeneratorService.getInstance().spawnItem(generator);
        }
    }
}
