package de.amr.basics.ecs.comp;

public class LifetimeComp {

    private long ticksRemaining;

    public LifetimeComp(long ticks) {
        ticksRemaining = ticks;
    }

    public long ticksRemaining() {
        return ticksRemaining;
    }

    public void becomeOlder() {
        --ticksRemaining;
    }

    public boolean ends() {
        return ticksRemaining <= 0;
    }
}
