/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;

import static java.util.Objects.requireNonNull;

public class GhostStateComp implements EntityComponent {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private GhostState enumValue;

    private boolean flashing;

    private boolean threatenedByPac;

    public GhostStateComp() {
        reset();
    }

    public GhostState enumValue() {
        return enumValue;
    }

    public void setEnumValue(GhostState enumValue) {
        this.enumValue = requireNonNull(enumValue);
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
        enumValue = DEFAULT_STATE;
        flashing = false;
        threatenedByPac = false;
    }
}
