/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;

import java.util.Objects;

public class PacStateComp implements EntityComponent {

    private PacState enumValue;

    private boolean moving;

    private boolean male;

    public PacState enumValue() {
        return enumValue;
    }

    public void setEnumValue(PacState enumValue) {
        this.enumValue = Objects.requireNonNull(enumValue);
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    @Override
    public void reset() {
        enumValue = PacState.SLEEPING;
        moving = false;
    }
}
