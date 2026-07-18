package it.unicam.cs.mpgc.rpg122830.core.model;

import it.unicam.cs.mpgc.rpg122830.core.exception.InsufficientFundsException;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int cash;
    private int reputation;
    private final List<Vehicle> vehicles;
    private final List<Component> inventory;

    public Player() {
        this.cash = 10000; // Starting cash
        this.reputation = 0;
        this.vehicles = new ArrayList<>();
        this.inventory = new ArrayList<>();
    }

    public int getCash() {
        return cash;
    }

    public void addCash(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative cash.");
        }
        this.cash += amount;
    }

    public void deductCash(int amount) throws InsufficientFundsException {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot deduct negative cash.");
        }
        if (this.cash < amount) {
            throw new InsufficientFundsException(
                String.format("Insufficient funds! Required: $%d, Available: $%d", amount, this.cash)
            );
        }
        this.cash -= amount;
    }

    public int getReputation() {
        return reputation;
    }

    public void addReputation(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative reputation.");
        }
        this.reputation += amount;
    }

    public int getLevel() {
        return 1 + (this.reputation / 100);
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void addVehicle(Vehicle vehicle) {
        if (vehicle != null && !vehicles.contains(vehicle)) {
            vehicles.add(vehicle);
        }
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }

    public List<Component> getInventory() {
        return inventory;
    }

    public void addComponent(Component component) {
        if (component != null) {
            inventory.add(component);
        }
    }

    public void removeComponent(Component component) {
        inventory.remove(component);
    }

    @Override
    public String toString() {
        return String.format("Player Info - Cash: $%d | Reputation: %d (Level %d) | Owned Vehicles: %d | Spare Components: %d",
                cash, reputation, getLevel(), vehicles.size(), inventory.size());
    }
}
