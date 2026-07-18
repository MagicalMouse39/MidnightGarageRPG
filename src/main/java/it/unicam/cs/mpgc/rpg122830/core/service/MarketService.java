package it.unicam.cs.mpgc.rpg122830.core.service;

import it.unicam.cs.mpgc.rpg122830.core.exception.InsufficientFundsException;
import it.unicam.cs.mpgc.rpg122830.core.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarketService {
    private final List<Vehicle> availableVehicles = new ArrayList<>();
    private final List<Component> availableComponents = new ArrayList<>();
    private final Random random = new Random();

    public MarketService() {
        refreshMarket(1);
    }

    public List<Vehicle> getAvailableVehicles() {
        return availableVehicles;
    }

    public List<Component> getAvailableComponents() {
        return availableComponents;
    }

    /**
     * Refreshes the market items. Generates cars and spare parts.
     * Higher player level leads to better components appearing.
     */
    public void refreshMarket(int playerLevel) {
        availableVehicles.clear();
        availableComponents.clear();

        // 1. Generate Vehicles
        String[] models = {"Honda Civic Si", "Toyota AE86 Trueno", "Mazda RX-7 FD", "Nissan Skyline GT-R", "Subaru Impreza WRX", "Mitsubishi Eclipse GSX"};
        int[] years = {1998, 1985, 1994, 1999, 2002, 1968};
        int[] baseValues = {4000, 8000, 15000, 28000, 11000, 18000};

        // Pick 3 random vehicles
        List<Integer> chosenIndices = new ArrayList<>();
        while (chosenIndices.size() < 3) {
            int idx = random.nextInt(models.length);
            if (!chosenIndices.contains(idx)) {
                chosenIndices.add(idx);
            }
        }

        for (int idx : chosenIndices) {
            Vehicle car = new Vehicle(models[idx], years[idx], baseValues[idx]);
            // Give each shop car some random OEM components with wear
            for (ComponentType type : ComponentType.values()) {
                if (random.nextDouble() > 0.15) { // 85% chance to have the component installed
                    car.installComponent(ComponentFactory.createOEMComponent(type));
                }
            }
            availableVehicles.add(car);
        }

        // 2. Generate Components
        for (ComponentType type : ComponentType.values()) {
            // One rusty/cheap component of this type
            availableComponents.add(ComponentFactory.createRustyComponent(type));

            // Chance to generate a racing component, higher chance if player level is higher
            double racingChance = 0.15 + (playerLevel * 0.10);
            if (random.nextDouble() < racingChance) {
                availableComponents.add(ComponentFactory.createRacingComponent(type));
            } else {
                availableComponents.add(ComponentFactory.createOEMComponent(type));
            }
        }
    }

    public void buyVehicle(Player player, Vehicle vehicle) throws InsufficientFundsException {
        int price = vehicle.getCurrentValue();
        player.deductCash(price);
        player.addVehicle(vehicle);
        availableVehicles.remove(vehicle);
    }

    public void buyComponent(Player player, Component component) throws InsufficientFundsException {
        int price = getComponentPrice(component);
        player.deductCash(price);
        player.addComponent(component);
        availableComponents.remove(component);
    }

    public void sellVehicle(Player player, Vehicle vehicle) {
        // Sell for 75% of its current condition-adjusted value
        int payout = (int) (vehicle.getCurrentValue() * 0.75);
        player.addCash(payout);
        player.removeVehicle(vehicle);
    }

    public void sellComponent(Player player, Component component) {
        // Sell for 50% of the brand-new component price
        int payout = (int) (getComponentPrice(component) * 0.50 * component.getCondition());
        player.addCash(Math.max(10, payout));
        player.removeComponent(component);
    }

    public int getComponentPrice(Component component) {
        // Base price calculation based on performance bonus and type
        int base = component.getPerformanceBonus() * 10;
        return (int) (base * (0.5 + 0.5 * component.getCondition()));
    }
}
