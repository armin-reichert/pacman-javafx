/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.pac.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.model.entities.pac.PacState;

import java.util.Objects;

public class PacStateComp implements GameEntityComponent {

    private PacState state;

    private boolean blocked;

    public PacState pacState() {
        return state;
    }

    public void setState(PacState state) {
        this.state = Objects.requireNonNull(state);
    }

    public boolean blocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public void reset() {
        state = PacState.ACTIVE;
    }
}
