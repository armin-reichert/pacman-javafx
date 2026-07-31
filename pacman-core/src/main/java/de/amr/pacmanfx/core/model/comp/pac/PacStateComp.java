/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.comp.pac;

import de.amr.pacmanfx.core.model.GameEntityComponent;
import de.amr.pacmanfx.core.model.actors.PacState;

import java.util.Objects;

public class PacStateComp implements GameEntityComponent {

    private PacState state;

    public PacState state() {
        return state;
    }

    public void setState(PacState state) {
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public void reset() {
        state = PacState.ACTIVE;
    }
}
