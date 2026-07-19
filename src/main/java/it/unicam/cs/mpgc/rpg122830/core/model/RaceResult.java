package it.unicam.cs.mpgc.rpg122830.core.model;

public class RaceResult {
    private final boolean won;
    private final String narrativeLog;
    private final int rewardCash;
    private final int rewardReputation;
    private final String raceName;

    public RaceResult(String raceName, boolean won, String narrativeLog, int rewardCash, int rewardReputation) {
        this.raceName = raceName;
        this.won = won;
        this.narrativeLog = narrativeLog;
        this.rewardCash = rewardCash;
        this.rewardReputation = rewardReputation;
    }

    public String getRaceName() {
        return raceName;
    }

    public boolean isWon() {
        return won;
    }

    public String getNarrativeLog() {
        return narrativeLog;
    }

    public int getRewardCash() {
        return rewardCash;
    }

    public int getRewardReputation() {
        return rewardReputation;
    }

    @Override
    public String toString() {
        return String.format("%s - %s! Reward: $%d, +%d XP", 
                raceName, won ? "WON" : "LOST", rewardCash, rewardReputation);
    }
}
