package com.gungens.generators.commands;

import com.gungens.generators.Generators;
import com.gungens.generators.cache.BreakableGeneratorCache;
import com.gungens.generators.cache.GeneratorCache;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class ReloadAll implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender.hasPermission("generators.reload")) {
            sender.sendMessage(ChatColor.GREEN+"Reloading generators...");
            Generators.instance.reloadConfig();
            try {
                Generators.instance.getDbManager().flushDirtyGenerators();
                BreakableGeneratorCache.instance.clearDirtyAndRemoved();
                GeneratorCache.instance.clearDirtyAndRemoved();
                Generators.instance.getDbManager().loadGenerators();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            sender.sendMessage(ChatColor.GREEN+"Generators reloaded!");
        }
        return true;
    }
}
