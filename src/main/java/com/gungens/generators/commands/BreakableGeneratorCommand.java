
package com.gungens.generators.commands;

import com.gungens.generators.libs.ItemUtils;
import com.gungens.generators.libs.MessageUtils;
import com.gungens.generators.models.BreakableGenerator;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BreakableGeneratorCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /breakablegenerator <block>");
            return true;
        }

        String resource = args[0].toUpperCase();
        Material material;
        try {
            material = Material.valueOf(resource);
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid material: " + args[0]);
            return true;
        }

        if (!material.isBlock()) {
            player.sendMessage(args[0] + " is not a valid block.");
            return true;
        }

        MessageUtils utils = MessageUtils.instance;

        // Create a new breakable generator
        BreakableGenerator breakableGenerator = new BreakableGenerator(material);
        breakableGenerator.setOwnerUUID(player.getUniqueId().toString());

        // Create the block item to give to the player
        ItemStack generatorBlock = ItemUtils.instance.createBreakableGeneratorItem(material, player.getName(), breakableGenerator);

        player.getInventory().addItem(generatorBlock);
        player.sendMessage(utils.format("&aBreakable Generator created with block: " + material.name()));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (Material material : Material.values()) {
                if (material.isBlock() && material.name().toLowerCase().startsWith(input)) {
                    completions.add(material.name().toLowerCase());
                }
            }
        }
        return completions;
    }
}