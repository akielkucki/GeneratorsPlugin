package com.gungens.generators.models;

public interface InventoryState {
    double getInterval();
    CommandState getCommandState();
    void setState(CommandState state, double interval);

}
