package it.unicam.cs.mpgc.rpg122830.core.service;

import it.unicam.cs.mpgc.rpg122830.core.model.*;
import java.util.Random;

public class DriftRaceStrategy implements RaceStrategy {
    private static final Random random = new Random();
    private final int playerLevel;

    public DriftRaceStrategy(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    @Override
    public String getRaceName() {
        return "Touge Mountain Drift";
    }

    @Override
    public String getDescription() {
        return "A technical, twisty mountain run. Requires excellent Suspension, Brakes, and driver skill.";
    }

    @Override
    public String toString() {
        return getRaceName();
    }

    @Override
    public RaceResult executeRace(Vehicle vehicle) {
        StringBuilder log = new StringBuilder();
        log.append("=== TOUGE DRIFT BATTLE ===\n");
        log.append(String.format("Car: %d %s (Driver Level %d)\n", vehicle.getYear(), vehicle.getModelName(), playerLevel));

        Component suspension = vehicle.getComponent(ComponentType.SUSPENSION);
        Component brakes = vehicle.getComponent(ComponentType.BRAKES);

        int suspBonus = (suspension != null) ? suspension.getEffectiveBonus() : 0;
        int brakesBonus = (brakes != null) ? brakes.getEffectiveBonus() : 0;
        int skillBonus = playerLevel * 8;

        int driftScore = suspBonus + brakesBonus + skillBonus;
        log.append(String.format("System Check: Suspension (+%d handling), Brakes (+%d stability), Skill (+%d score).\n", 
                suspBonus, brakesBonus, skillBonus));
        log.append(String.format("Total Drift Capability: %d points.\n\n", driftScore));

        if (suspension == null) {
            log.append("WARNING: No suspension installed! Your car scraping the floor makes a horrible sound...\n");
        }
        if (brakes == null) {
            log.append("WARNING: No brakes installed! Attempting to drift without brakes is suicide!\n");
        }

        int opponentDrift = 35 + random.nextInt(55) + (playerLevel * 3); // Scales with level slightly
        log.append(String.format("A local driver in a drift-spec coupe challenges you (Est. Drift Score: %d).\n", opponentDrift));
        log.append("The battle begins on the downhill touge!\n\n");

        log.append("Stage 1: The First Hairpin\n");
        if (suspension == null || suspension.getCondition() < 0.2) {
            log.append("Your car understeers heavily, sliding close to the guardrail!\n");
        } else {
            log.append(String.format("You initiate a clean drift. The %s keeps the car perfectly balanced.\n", suspension.getName()));
        }

        log.append("\nStage 2: Transistion and Clipping Points\n");
        if (brakes == null || brakes.getCondition() < 0.2) {
            log.append("Without reliable brakes, your entry speed is way too high. You overshoot the line.\n");
        } else {
            log.append(String.format("A quick tap on the %s pivots the car. You hit the inner clipping point perfectly.\n", brakes.getName()));
        }

        log.append("\nStage 3: The Final Corner\n");
        if (playerLevel > 3) {
            log.append("Your experienced countersteering keeps the rear out in an impressive high-angle slide!\n");
        } else {
            log.append("You fight the steering wheel to maintain control, surviving the run.\n");
        }

        boolean won = driftScore > opponentDrift;

        // Apply wear
        if (suspension != null) {
            double wear = 0.03 + (random.nextDouble() * 0.05); // 3% to 8% wear
            suspension.setCondition(suspension.getCondition() - wear);
            log.append(String.format("Suspension wear: -%.0f%% condition.\n", wear * 100));
        }
        if (brakes != null) {
            double wear = 0.02 + (random.nextDouble() * 0.04); // 2% to 6% wear
            brakes.setCondition(brakes.getCondition() - wear);
            log.append(String.format("Brakes wear: -%.0f%% condition.\n", wear * 100));
        }

        int rewardCash;
        int rewardRep;

        if (won) {
            log.append("\nYou linked the entire downhill section and finished ahead! Victory!\n");
            rewardCash = 500 + random.nextInt(250); // $500 - $750
            rewardRep = 60 + random.nextInt(40);   // 60 - 100 XP
        } else {
            log.append("\nThe opponent showed cleaner angles and took the win. Better luck next time!\n");
            rewardCash = 120;
            rewardRep = 20;
        }

        return new RaceResult(getRaceName(), won, log.toString(), rewardCash, rewardRep);
    }
}
