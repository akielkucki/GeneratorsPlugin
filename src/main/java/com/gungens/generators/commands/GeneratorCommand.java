package com.gungens.generators.commands;

import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.libs.ItemUtils;
import com.gungens.generators.libs.MessageUtils;
import com.gungens.generators.models.Generator;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GeneratorCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /generator <block>");
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

        Generator generator = new Generator(material);

        //Starting drop item
        ItemStack dropItem = ItemUtils.instance.createDropItem();
        generator.setDropItems(new ArrayList<>(Collections.singletonList(dropItem)));

        //Block to add to player's inventory
        ItemStack generatorBlock = ItemUtils.instance.createGeneratorItem(material,player.getName(),generator);

        player.getInventory().addItem(generatorBlock);
        player.sendMessage(utils.format("&aGenerator created with block: " + material.name()));

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
