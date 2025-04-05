package com.gungens.generators.libs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A builder class to easily create GUI inventories for managing generator blocks.
 * This builder supports setting items with optional click actions.
 *
 * Usage:
 * <pre>
 *   Inventory gui = new InventoryBuilder(27, "Generator Manager")
 *       .setItem(11, someItemStack, event -> {
 *           // handle click on slot 11
 *       })
 *       .setItem(15, anotherItemStack, event -> {
 *           // handle click on slot 15
 *       })
 *       .build();
 * </pre>
 *
 * In your InventoryClickEvent listener, call {@link #handleClick(InventoryClickEvent)}.
 */
public class InventoryBuilder {

    private final Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();

    // Static registry to keep track of inventories built with actions.
    private static final Map<Inventory, Map<Integer, Consumer<InventoryClickEvent>>> registeredInventories = new HashMap<>();

    /**
     * Constructs a new InventoryBuilder.
     *
     * @param size  The size of the inventory (must be a multiple of 9)
     * @param title The title of the inventory
     */
    public InventoryBuilder(int size, String title) {
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    /**
     * Sets an item at the specified slot with an associated click action.
     *
     * @param slot   The slot index in the inventory
     * @param item   The ItemStack to set
     * @param action The click action to execute when the slot is clicked
     * @return The current InventoryBuilder for chaining
     */
    public InventoryBuilder setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        actions.put(slot, action);
        return this;
    }

    public InventoryBuilder addItem(ItemStack item) {
        inventory.addItem(item);
        return this;
    }

    public boolean isInRegisteredInventories(Inventory inventory) {
        return registeredInventories.containsKey(inventory);
    }

    /**
     * Sets an item at the specified slot without a click action.
     *
     * @param slot The slot index in the inventory
     * @param item The ItemStack to set
     * @return The current InventoryBuilder for chaining
     */
    public InventoryBuilder setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
        return this;
    }

    /**
     * Finalizes the inventory and registers it for click handling.
     *
     * @return The built Inventory
     */
    public Inventory build() {
        registeredInventories.put(inventory, actions);
        return inventory;
    }

    /**
     * Handles an InventoryClickEvent if the clicked inventory was built by this builder.
     * If a registered click action exists for the slot, it is executed.
     *
     * @param event The InventoryClickEvent from your listener
     * @return true if an action was handled; false otherwise
     */
    public static boolean handleClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (registeredInventories.containsKey(inv)) {
            int slot = event.getRawSlot();
            Map<Integer, Consumer<InventoryClickEvent>> slotActions = registeredInventories.get(inv);
            if (slotActions.containsKey(slot)) {
                event.setCancelled(true); // prevent item pickup/movement
                slotActions.get(slot).accept(event);
                return true;
            }
        }
        return false;
    }

    /**
     * Optionally unregisters an inventory from the builder registry.
     *
     * @param inventory The inventory to unregister
     */
    public static void unregister(Inventory inventory) {
        registeredInventories.remove(inventory);
    }
}
