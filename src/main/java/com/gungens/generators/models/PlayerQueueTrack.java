package com.gungens.generators.models;

public class PlayerQueueTrack implements com.gungens.generators.models.InventoryState {
    private double interval;
    private CommandState commandState = CommandState.WAITING;
    @Override
    public double getInterval() {
        return interval;
    }

    @Override
    public CommandState getCommandState() {
        return commandState;
    }

    @Override
    public void setState(CommandState state, double interval) {
        this.commandState = state;
        this.interval = interval;
    }
}
