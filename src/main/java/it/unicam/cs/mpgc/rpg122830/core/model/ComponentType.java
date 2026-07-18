package it.unicam.cs.mpgc.rpg122830.core.model;

public enum ComponentType {
    ENGINE("Engine"),
    TURBOCHARGER("Turbocharger"),
    BRAKES("Brakes"),
    SUSPENSION("Suspension");

    private final String displayName;

    ComponentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
