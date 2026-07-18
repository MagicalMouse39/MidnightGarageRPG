package it.unicam.cs.mpgc.rpg122830.core.model;

public class Suspension extends Component {
    public Suspension(String name, double condition, int performanceBonus) {
        super(name, ComponentType.SUSPENSION, condition, performanceBonus);
    }
}
