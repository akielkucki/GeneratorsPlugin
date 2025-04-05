package com.gungens.generators.commands;

import com.gungens.generators.Generators;
import com.gungens.generators.tasks.DirtyGeneratorSync;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SaveGeneratorsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        DirtyGeneratorSync dirtyGeneratorSync = new DirtyGeneratorSync();
        Bukkit.getScheduler().runTaskAsynchronously(Generators.instance, dirtyGeneratorSync);
        if (dirtyGeneratorSync.isSaved()) {
            sender.sendMessage(ChatColor.GREEN + "Saved generators successfully!");
        } else {
            sender.sendMessage(ChatColor.RED + "There are no generators to save!");
        }

        return true;
    }
}
