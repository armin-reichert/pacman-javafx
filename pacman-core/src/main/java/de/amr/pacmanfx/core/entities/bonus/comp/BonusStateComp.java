/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.comp;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.ecs.EntityComponent;

import java.util.Objects;

public class BonusStateComp implements EntityComponent {

    private boolean edibleStateExpired;

    private BonusState enumValue;

    private final TickTimer timer;

    public BonusStateComp() {
        this.timer = new TickTimer("Bonus-Timer");    }

    public void setEnumValue(BonusState enumValue) {
        this.enumValue = Objects.requireNonNull(enumValue);
    }

    public BonusState enumValue() {
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
        enumValue = BonusState.INACTIVE;
    }
}
