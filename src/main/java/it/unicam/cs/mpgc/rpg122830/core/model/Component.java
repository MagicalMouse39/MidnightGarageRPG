package it.unicam.cs.mpgc.rpg122830.core.model;

public abstract class Component {
    private final String name;
    private final ComponentType type;
    private double condition; // between 0.0 and 1.0
    private final int performanceBonus;

    protected Component(String name, ComponentType type, double condition, int performanceBonus) {
        this.name = name;
        this.type = type;
        setCondition(condition);
        this.performanceBonus = performanceBonus;
    }

    public String getName() {
        return name;
    }

    public ComponentType getType() {
        return type;
    }

    public double getCondition() {
        return condition;
    }

    public void setCondition(double condition) {
        if (condition < 0.0) {
            this.condition = 0.0;
        } else if (condition > 1.0) {
            this.condition = 1.0;
        } else {
            this.condition = condition;
        }
    }

    public int getPerformanceBonus() {
        return performanceBonus;
    }

    /**
     * Calculates the effective performance contribution based on current condition.
     * Degraded components provide less bonus.
     */
    public int getEffectiveBonus() {
        return (int) (performanceBonus * condition);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Condition: %.0f%% - Bonus: +%d HP/Stats", 
                name, type.getDisplayName(), condition * 100, getEffectiveBonus());
    }
}
