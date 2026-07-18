package it.unicam.cs.mpgc.rpg122830.core.service;

import it.unicam.cs.mpgc.rpg122830.core.model.*;
import java.util.Random;

public class ComponentFactory {
    private static final Random random = new Random();

    public static Component createRustyComponent(ComponentType type) {
        double condition = 0.10 + (random.nextDouble() * 0.20); // 10% to 30% condition
        String name;
        int bonus;

        switch (type) {
            case ENGINE:
                name = getRandomElement(new String[]{"Rusty Inline-4", "Oil-Burning V6", "Junkyard Boxer-4"});
                bonus = 15 + random.nextInt(15); // 15 to 29
                return new Engine(name, condition, bonus);
            case TURBOCHARGER:
                name = getRandomElement(new String[]{"Sputtering T25", "Smoky Boost-Unit", "Clogged Wastegate"});
                bonus = 8 + random.nextInt(10); // 8 to 17
                return new Turbocharger(name, condition, bonus);
            case BRAKES:
                name = getRandomElement(new String[]{"Squealing Drums", "Worn Discs", "Rusty Calipers"});
                bonus = 5 + random.nextInt(10);
                return new Brakes(name, condition, bonus);
            case SUSPENSION:
                name = getRandomElement(new String[]{"Sagging Springs", "Leaky Dampers", "Creaking Bushings"});
                bonus = 5 + random.nextInt(10);
                return new Suspension(name, condition, bonus);
            default:
                throw new IllegalArgumentException("Unknown component type: " + type);
        }
    }

    public static Component createRacingComponent(ComponentType type) {
        double condition = 0.90 + (random.nextDouble() * 0.10); // 90% to 100% condition
        String name;
        int bonus;

        switch (type) {
            case ENGINE:
                name = getRandomElement(new String[]{"4G63T", "2JZ GTE", "Billet Rotary Engine", "Fiat 1.4 T-JET"});
                bonus = 90 + random.nextInt(31); // 90 to 120
                return new Engine(name, condition, bonus);
            case TURBOCHARGER:
                name = getRandomElement(new String[]{"Garrett GT35R", "BorgWarner EFR", "HKS Twin-Scroll"});
                bonus = 45 + random.nextInt(16); // 45 to 60
                return new Turbocharger(name, condition, bonus);
            case BRAKES:
                name = getRandomElement(new String[]{"Brembo Carbon-Ceramics", "6-Piston AP Racing", "Endurance Brembo Kit"});
                bonus = 30 + random.nextInt(16);
                return new Brakes(name, condition, bonus);
            case SUSPENSION:
                name = getRandomElement(new String[]{"Ohlins TTX Coilovers", "Bilstein Clubsport", "KW Variant 3"});
                bonus = 30 + random.nextInt(16);
                return new Suspension(name, condition, bonus);
            default:
                throw new IllegalArgumentException("Unknown component type: " + type);
        }
    }

    /**
     * Create a standard OEM component (useful for default/starting cars)
     */
    public static Component createOEMComponent(ComponentType type) {
        double condition = 0.50 + (random.nextDouble() * 0.20); // 50% to 70% condition
        String name;
        int bonus;

        switch (type) {
            case ENGINE:
                name = "Stock Factory Engine";
                bonus = 40;
                return new Engine(name, condition, bonus);
            case TURBOCHARGER:
                name = "Stock Turbocharger";
                bonus = 20;
                return new Turbocharger(name, condition, bonus);
            case BRAKES:
                name = "Stock Brakes";
                bonus = 15;
                return new Brakes(name, condition, bonus);
            case SUSPENSION:
                name = "Stock Suspension";
                bonus = 15;
                return new Suspension(name, condition, bonus);
            default:
                throw new IllegalArgumentException("Unknown component type: " + type);
        }
    }

    private static String getRandomElement(String[] array) {
        return array[random.nextInt(array.length)];
    }
}
