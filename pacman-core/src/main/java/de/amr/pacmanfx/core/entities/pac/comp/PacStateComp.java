/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

import java.util.Objects;

public class PacStateComp implements GameEntityComp {

    private PacState enumValue;

    private final boolean male;

    public PacStateComp(boolean male) {
        this.male = male;
        reset();
    }

    public PacState enumValue() {
        return enumValue;
    }

    public void setEnumValue(PacState enumValue) {
        this.enumValue = Objects.requireNonNull(enumValue);
    }

    public boolean isMale() {
        return male;
    }

    @Override
    public void reset() {
        enumValue = PacState.SLEEPING;
    }
}
