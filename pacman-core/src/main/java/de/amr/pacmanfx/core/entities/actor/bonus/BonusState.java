/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.actor.bonus;

import de.amr.basics.ecs.Resettable;
import de.amr.basics.timer.TickTimer;

import java.util.Objects;

public class BonusState implements Resettable {

    private boolean edibleStateExpired;

    private BonusStateEnum enumValue;

    private final TickTimer timer;

    public BonusState() {
        this.timer = new TickTimer("Bonus-Timer");    }

    public void setEnumValue(BonusStateEnum enumValue) {
        this.enumValue = Objects.requireNonNull(enumValue);
    }

    public BonusStateEnum enumValue() {
        return enumValue;
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
        enumValue = BonusStateEnum.INACTIVE;
    }
}
