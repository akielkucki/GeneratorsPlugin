package com.gungens.generators.tasks;

import com.gungens.generators.Generators;
import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.db.DbManager;
import com.gungens.generators.libs.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class DirtyGeneratorSync implements Runnable {
    final DbManager dbManager = Generators.instance.getDbManager();
    private boolean saved;
    public DirtyGeneratorSync() {
        saved = false;
    }
    @Override
    public void run() {
        if (GeneratorCache.instance.getRemovedGenerators().isEmpty() &&
                GeneratorCache.instance.getDirtyGenerators().isEmpty()) {
            saved = false;
            return;
        }

        try {
            dbManager.flushDirtyGenerators();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, e.getMessage());
        }
        saved = true;
        sendMessage();
    }
    private void sendMessage() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("generators.logs")) {
                player.sendMessage(MessageUtils.instance.format("&7&oSaving generator data..."));
            }
        }
    }
    public boolean isSaved() {
        return saved;
    }
}
