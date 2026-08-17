/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;

import java.util.Objects;

public class PacStateComp implements EntityComponent {

    private PacState state;

    private boolean moving;

    private boolean male;

    public PacState enumValue() {
        return state;
    }

    public void setState(PacState state) {
        this.state = Objects.requireNonNull(state);
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
        state = PacState.ACTIVE;
        moving = false;
    }
}
