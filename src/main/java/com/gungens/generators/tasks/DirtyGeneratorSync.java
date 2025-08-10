package com.gungens.generators.tasks;

import com.gungens.generators.Generators;
import com.gungens.generators.cache.BreakableGeneratorCache;
import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.db.DbManager;
import com.gungens.generators.libs.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DirtyGeneratorSync implements Runnable {
    private final DbManager dbManager = Generators.instance.getDbManager();
    private volatile boolean saved;
    private long lastLogNanos = 0L;
    private static final long LOG_COOLDOWN = TimeUnit.SECONDS.toNanos(10);

    @Override
    public void run() {
        // snapshot counts before flush (db flush is expected to clear caches)
        int gDirty = GeneratorCache.instance.getDirtyGenerators().size();
        int gRemoved = GeneratorCache.instance.getRemovedGenerators().size();
        int bDirty = BreakableGeneratorCache.instance.getDirtyBreakableGenerators().size();
        int bRemoved = BreakableGeneratorCache.instance.getRemovedBreakableGenerators().size();

        int total = gDirty + gRemoved + bDirty + bRemoved;
        if (total == 0) { saved = false; return; }

        try {
            dbManager.flushDirtyGenerators();
            saved = true;
            if (Generators.instance.getConfig().getBoolean("database_logging")) {
                maybeNotifyAdmins(gDirty, gRemoved, bDirty, bRemoved);
            }
        } catch (SQLException e) {
            saved = false;
            Generators.instance.getLogger().log(Level.SEVERE, "Failed to flush dirty generators", e);
        }
    }

    private void maybeNotifyAdmins(int gDirty, int gRemoved, int bDirty, int bRemoved) {
        long now = System.nanoTime();
        if (now - lastLogNanos < LOG_COOLDOWN) return;
        lastLogNanos = now;

        String msg = MessageUtils.instance.format(
                String.format("&7&oSaving generator data... &8G[d=%d r=%d] BG[d=%d r=%d]",
                        gDirty, gRemoved, bDirty, bRemoved)
        );

        // Bounce to main thread (Bukkit API is not thread-safe from async tasks)
        Bukkit.getScheduler().runTask(Generators.instance, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("generators.logs")) p.sendMessage(msg);
            }
        });
    }

    public boolean isSaved() { return saved; }
}
