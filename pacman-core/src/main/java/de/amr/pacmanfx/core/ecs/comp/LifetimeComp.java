package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

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
