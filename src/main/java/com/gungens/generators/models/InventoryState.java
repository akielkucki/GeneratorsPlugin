package com.gungens.generators.models;

import com.gungens.generators.libs.InventoryBuilder;
import org.bukkit.inventory.Inventory;

public interface InventoryState {
    double getInterval();
    CommandState getCommandState();
    void setState(CommandState state, double interval);

}
