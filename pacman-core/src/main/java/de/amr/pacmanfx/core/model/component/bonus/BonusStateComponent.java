/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component.bonus;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.model.GameEntityComponent;

import java.util.Objects;

public class BonusStateComponent implements GameEntityComponent {

    private boolean edibleStateExpired;

    private BonusState state;

    private final TickTimer timer;

    public BonusStateComponent() {
        this.timer = new TickTimer("Bonus-Timer");    }

    public void setState(BonusState state) {
        this.state = Objects.requireNonNull(state);
    }

    public BonusState state() {
        return state;
    }

    public TickTimer timer() {
        return timer;
    }

    public boolean edibleStateExpired() {
        return edibleStateExpired;
    }

    public void setEdibleStateExpired(boolean edibleStateExpired) {
        this.edibleStateExpired = edibleStateExpired;
    }

    @Override
    public void reset() {
        edibleStateExpired = false;
        state = BonusState.INACTIVE;
    }
}
