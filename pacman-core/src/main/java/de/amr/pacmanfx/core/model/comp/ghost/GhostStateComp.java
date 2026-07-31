/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.comp.ghost;

import de.amr.pacmanfx.core.model.GameEntityComponent;

import static java.util.Objects.requireNonNull;

public class GhostStateComp implements GameEntityComponent {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private GhostState state;

    public GhostStateComp() {
        state = DEFAULT_STATE;
    }

    public GhostState state() {
        return state;
    }

    public void setState(GhostState state) {
        this.state = requireNonNull(state);
    }

    @Override
    public void reset() {
        state = DEFAULT_STATE;
    }
}
