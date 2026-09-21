package de.amr.basics.ui.ecs.comp;

import de.amr.basics.ui.ecs.GameEntityComp;

public class LifetimeComp implements GameEntityComp {

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
