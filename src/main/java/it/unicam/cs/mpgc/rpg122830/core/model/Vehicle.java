package it.unicam.cs.mpgc.rpg122830.core.model;

import java.util.HashMap;
import java.util.Map;

public class Vehicle {
    private final String modelName;
    private final int year;
    private final int baseValue;
    private final Map<ComponentType, Component> installedComponents;

    public Vehicle(String modelName, int year, int baseValue) {
        this.modelName = modelName;
        this.year = year;
        this.baseValue = baseValue;
        this.installedComponents = new HashMap<>();
    }

    public String getModelName() {
        return modelName;
    }

    public int getYear() {
        return year;
    }

    public int getBaseValue() {
        return baseValue;
    }

    public Map<ComponentType, Component> getInstalledComponents() {
        return installedComponents;
    }

    public Component getComponent(ComponentType type) {
        return installedComponents.get(type);
    }

    /**
     * Installs a component in the vehicle.
     * @param component the component to install
     * @return the old component of the same type if one was already installed, or null
     */
    public Component installComponent(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("Cannot install a null component.");
        }
        return installedComponents.put(component.getType(), component);
    }

    /**
     * Calculates the average condition of all required components (Engine, Turbo, Brakes, Suspension).
     * If a component is missing, it counts as 0.0 condition.
     */
    public double getAverageCondition() {
        double sum = 0.0;
        for (ComponentType type : ComponentType.values()) {
            Component comp = installedComponents.get(type);
            if (comp != null) {
                sum += comp.getCondition();
            }
        }
        return sum / ComponentType.values().length;
    }

    /**
     * Computes the current market value of the vehicle based on its components' conditions.
     */
    public int getCurrentValue() {
        return (int) (baseValue * getAverageCondition());
    }

    /**
     * Calculates the total performance score of the vehicle.
     */
    public int getPerformanceScore() {
        int score = 0;
        for (Component comp : installedComponents.values()) {
            score += comp.getEffectiveBonus();
        }
        return score;
    }

    @Override
    public String toString() {
        return String.format("%d %s (Est. Value: $%d, Performance: +%d)", 
                year, modelName, getCurrentValue(), getPerformanceScore());
    }
}
