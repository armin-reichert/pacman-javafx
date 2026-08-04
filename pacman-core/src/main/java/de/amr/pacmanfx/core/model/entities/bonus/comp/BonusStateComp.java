/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.bonus.comp;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.model.entities.bonus.BonusState;

import java.util.Objects;

public class BonusStateComp implements GameEntityComponent {

    private boolean edibleStateExpired;

    private BonusState bonusState;

    private final TickTimer timer;

    public BonusStateComp() {
        this.timer = new TickTimer("Bonus-Timer");    }

    public void setBonusState(BonusState bonusState) {
        this.bonusState = Objects.requireNonNull(bonusState);
    }

    public BonusState bonusState() {
        return bonusState;
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
        bonusState = BonusState.INACTIVE;
    }
}
