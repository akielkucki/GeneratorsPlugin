package com.gungens.generators.models;

public class PlayerQueueTrack implements com.gungens.generators.models.InventoryState {
    private double interval;
    private CommandState commandState = CommandState.WAITING;
    private String commandName;
    @Override
    public double getInterval() {
        return interval;
    }

    @Override
    public CommandState getCommandState() {
        return commandState;
    }
    public String getCommandName() {
        return commandName;
    }
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
    public PlayerQueueTrack(String commandName) {
        this.commandName = commandName;
    }
    public PlayerQueueTrack(String commandName, double interval) {}

    @Override
    public void setState(CommandState state, double interval) {
        this.commandState = state;
        this.interval = interval;
    }
}
