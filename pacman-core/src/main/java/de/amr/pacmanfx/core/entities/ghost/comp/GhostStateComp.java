/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;

import static java.util.Objects.requireNonNull;

public class GhostStateComp implements EntityComponent {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private GhostState ghostStateEnum;

    private boolean flashing;

    private boolean threatenedByPac;

    public GhostStateComp() {
        reset();
    }

    public GhostState ghostStateEnum() {
        return ghostStateEnum;
    }

    public void setGhostStateEnum(GhostState ghostStateEnum) {
        this.ghostStateEnum = requireNonNull(ghostStateEnum);
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
        ghostStateEnum = DEFAULT_STATE;
        flashing = false;
        threatenedByPac = false;
    }
}
