package it.unicam.cs.mpgc.rpg122830.core.service;

import it.unicam.cs.mpgc.rpg122830.core.model.*;
import java.util.Random;

public class DragRaceStrategy implements RaceStrategy {
    private static final Random random = new Random();

    @Override
    public String getRaceName() {
        return "Midnight Drag Strip";
    }

    @Override
    public String getDescription() {
        return "A straight-line sprint. Power is king. Relies heavily on Engine and Turbocharger.";
    }

    @Override
    public String toString() {
        return getRaceName();
    }

    @Override
    public RaceResult executeRace(Vehicle vehicle) {
        StringBuilder log = new StringBuilder();
        log.append("=== MIDNIGHT DRAG RACE ===\n");
        log.append(String.format("Car: %d %s\n", vehicle.getYear(), vehicle.getModelName()));

        Component engine = vehicle.getComponent(ComponentType.ENGINE);
        Component turbo = vehicle.getComponent(ComponentType.TURBOCHARGER);

        int engineBonus = (engine != null) ? engine.getEffectiveBonus() : 0;
        int turboBonus = (turbo != null) ? turbo.getEffectiveBonus() : 0;

        int totalPower = engineBonus + turboBonus;
        log.append(String.format("System Check: Engine (+%d HP), Turbo (+%d HP). Total Drag Power: %d HP.\n\n", 
                engineBonus, turboBonus, totalPower));

        if (engine == null) {
            log.append("WARNING: No engine installed! You barely roll off the starting line...\n");
        }

        // Opponent difficulty (power score)
        int opponentPower = 40 + random.nextInt(60); // 40 to 100
        log.append(String.format("An opponent pulls up in a modified sports car (Est. Power: %d HP).\n", opponentPower));
        log.append("The lights go green! GO!\n\n");

        log.append("Stage 1: Launch!\n");
        if (engine == null) {
            log.append("Your car doesn't even move. The opponent screams ahead.\n");
        } else if (engine.getCondition() < 0.2) {
            log.append("Your engine sputters and misfires! Bad launch!\n");
        } else {
            log.append("Tires screech! You launch off the line with decent grip.\n");
        }

        log.append("\nStage 2: The Shift!\n");
        if (turbo != null && turbo.getCondition() > 0.5) {
            log.append(String.format("The %s spools up! Massive boost kicks in!\n", turbo.getName()));
        } else if (turbo != null) {
            log.append(String.format("The worn-out %s whines but struggles to deliver full boost.\n", turbo.getName()));
        } else {
            log.append("Naturally aspirated acceleration is smooth, but lacks that turbo punch.\n");
        }

        log.append("\nStage 3: The Finish!\n");
        boolean won = totalPower > opponentPower;

        // Apply wear
        if (engine != null) {
            double wear = 0.03 + (random.nextDouble() * 0.05); // 3% to 8% wear
            engine.setCondition(engine.getCondition() - wear);
            log.append(String.format("Engine wear: -%.0f%% condition.\n", wear * 100));
        }
        if (turbo != null) {
            double wear = 0.02 + (random.nextDouble() * 0.04); // 2% to 6% wear
            turbo.setCondition(turbo.getCondition() - wear);
            log.append(String.format("Turbo wear: -%.0f%% condition.\n", wear * 100));
        }

        int rewardCash;
        int rewardRep;

        if (won) {
            log.append("\nYou cross the finish line FIRST! Victory is yours!\n");
            rewardCash = 400 + random.nextInt(300); // $400 - $700
            rewardRep = 50 + random.nextInt(30);   // 50 - 80 XP
        } else {
            log.append("\nThe opponent pulls ahead and crosses the line first. You lose!\n");
            rewardCash = 100;
            rewardRep = 15;
        }

        return new RaceResult(getRaceName(), won, log.toString(), rewardCash, rewardRep);
    }
}
