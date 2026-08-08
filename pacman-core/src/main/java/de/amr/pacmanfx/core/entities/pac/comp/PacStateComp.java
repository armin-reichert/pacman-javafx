/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

import java.util.Objects;

public class PacStateComp implements GameEntityComponent {

    private PacState state;

    private boolean moving;

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

    @Override
    public void reset() {
        state = PacState.ACTIVE;
        moving = false;
    }
}
