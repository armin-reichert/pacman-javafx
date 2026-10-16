package de.amr.basics.ecs.comp;

public class Lifetime {

    private long ticksRemaining;

    public Lifetime(long ticks) {
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
