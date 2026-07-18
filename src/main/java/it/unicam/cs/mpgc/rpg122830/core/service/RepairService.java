package it.unicam.cs.mpgc.rpg122830.core.service;

import it.unicam.cs.mpgc.rpg122830.core.exception.InsufficientFundsException;
import it.unicam.cs.mpgc.rpg122830.core.model.Component;
import it.unicam.cs.mpgc.rpg122830.core.model.Player;

public class RepairService {

    /**
     * Calculates the cost to fully restore a component (to 100% condition).
     * The cost is proportional to the missing condition and the performance bonus of the component.
     */
    public int calculateRepairCost(Component component) {
        if (component == null || component.getCondition() >= 1.0) {
            return 0;
        }
        
        // Base cost factor depends on how high-performance the component is
        double missingCondition = 1.0 - component.getCondition();
        int baseCost = component.getPerformanceBonus() * 6; // e.g. a +100 bonus component costs up to $600 to repair
        
        // Ensure a minimum cost if there is wear
        return Math.max(15, (int) (missingCondition * baseCost));
    }

    /**
     * Repairs the component to 100% condition. Deducts cash from the player and increases player reputation.
     */
    public void repairComponent(Player player, Component component) throws InsufficientFundsException {
        if (component == null) {
            throw new IllegalArgumentException("Cannot repair a null component.");
        }
        
        if (component.getCondition() >= 1.0) {
            return; // Already fully repaired
        }

        int cost = calculateRepairCost(component);
        player.deductCash(cost);
        
        double repairedAmount = 1.0 - component.getCondition();
        component.setCondition(1.0);
        
        // Award XP (reputation) for doing mechanical repairs!
        int repGained = Math.max(5, (int) (repairedAmount * 40));
        player.addReputation(repGained);
    }
}
