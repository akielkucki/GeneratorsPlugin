package com.gungens.generators;

import com.gungens.generators.commands.GeneratorCommand;
import com.gungens.generators.commands.SaveGeneratorsCommand;
import com.gungens.generators.db.DbManager;
import com.gungens.generators.libs.Register;
import com.gungens.generators.tasks.DirtyGeneratorSync;
import com.gungens.generators.tasks.GeneratorTask;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;

public final class Generators extends JavaPlugin {
    public static Generators instance;
    private DbManager dbManager;
    @Override
    public void onEnable() {
        instance = this;
        dbManager = new DbManager();
        try {
            dbManager.loadGenerators();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        registerListeners("com.gungens.generators.listeners");
        getCommand("generator").setExecutor(new GeneratorCommand());
        getCommand("savegenerators").setExecutor(new SaveGeneratorsCommand());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this,new GeneratorTask(), 0L, 20L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new DirtyGeneratorSync(), 20 * 20, 20 * 10); //every 60 seconds
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    private void registerListeners(String packageToScan) {
        Reflections reflections = new Reflections(packageToScan);
        Set<Class<?>> listenerClasses = reflections.getTypesAnnotatedWith(Register.class);

        for (Class<?> clazz : listenerClasses) {
            if (Listener.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    Listener listener = (Listener) clazz.getDeclaredConstructor().newInstance();
                    getServer().getPluginManager().registerEvents(listener, this);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to register listener " + clazz.getName(), e);
                }
            }
        }
    }
    public DbManager getDbManager() {
        return dbManager;
    }
}
