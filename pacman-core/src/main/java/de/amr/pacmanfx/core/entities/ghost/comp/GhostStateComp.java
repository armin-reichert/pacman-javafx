/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

import static java.util.Objects.requireNonNull;

public class GhostStateComp implements GameEntityComponent {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private GhostState state;

    private boolean flashing;

    private boolean threatenedByPac;

    public GhostStateComp() {
        state = DEFAULT_STATE;
    }

    public GhostState state() {
        return state;
    }

    public void setState(GhostState state) {
        this.state = requireNonNull(state);
    }

    public boolean flashing() {
        return flashing;
    }

    public void setFlashing(boolean flashing) {
        this.flashing = flashing;
    }

    public boolean isThreatenedByPac() {
        return threatenedByPac;
    }

    public void setThreatenedByPac(boolean threatenedByPac) {
        this.threatenedByPac = threatenedByPac;
    }

    @Override
    public void reset() {
        state = DEFAULT_STATE;
        flashing = false;
    }
}
