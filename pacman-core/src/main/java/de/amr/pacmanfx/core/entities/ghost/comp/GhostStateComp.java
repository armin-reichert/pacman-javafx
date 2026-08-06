/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

import static java.util.Objects.requireNonNull;

public class GhostStateComp implements GameEntityComponent {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private GhostState stateValue;

    private boolean flashing;

    private boolean threatenedByPac;

    public GhostStateComp() {
        reset();
    }

    public GhostState stateValue() {
        return stateValue;
    }

    public void setStateValue(GhostState stateValue) {
        this.stateValue = requireNonNull(stateValue);
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
        stateValue = DEFAULT_STATE;
        flashing = false;
        threatenedByPac = false;
    }
}
