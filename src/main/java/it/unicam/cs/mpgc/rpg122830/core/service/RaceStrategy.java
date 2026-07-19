package it.unicam.cs.mpgc.rpg122830.core.service;

import it.unicam.cs.mpgc.rpg122830.core.model.RaceResult;
import it.unicam.cs.mpgc.rpg122830.core.model.Vehicle;

public interface RaceStrategy {
    RaceResult executeRace(Vehicle vehicle);
    String getRaceName();
    String getDescription();
}
