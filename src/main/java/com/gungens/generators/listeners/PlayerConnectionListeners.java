package com.gungens.generators.listeners;

import com.gungens.generators.Generators;
import com.gungens.generators.libs.Register;
import com.gungens.generators.tasks.GeneratorTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Register
public class PlayerConnectionListeners implements Listener {
    private static final Object taskLock = new Object();
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        synchronized (taskLock) {
            if (Generators.instance.generatorsTask == -1) {
                int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Generators.instance, new GeneratorTask(), 0L, 1L);
                Generators.instance.setTaskId(taskId);
                Bukkit.getLogger().info("[Generators] Started generator task.");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(Generators.instance, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty() && Generators.instance.generatorsTask != -1) {
                Bukkit.getScheduler().cancelTask(Generators.instance.generatorsTask);
                Generators.instance.setTaskId(-1);
                Bukkit.getLogger().info("[Generators] Stopped generator task.");

            }
        }, 20L);
    }


}
