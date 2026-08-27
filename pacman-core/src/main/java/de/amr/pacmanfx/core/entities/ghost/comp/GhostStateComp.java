/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

import static java.util.Objects.requireNonNull;

public class GhostStateComp implements GameEntityComp {

    private GhostState enumValue;

    private long stateTick;

    private boolean flashing;

    private boolean threatenedByPac;

    private boolean killed;

    public GhostStateComp() {
        reset();
    }

    public GhostState enumValue() {
        return enumValue;
    }

    public void setEnumValue(GhostState enumValue) {
        this.enumValue = requireNonNull(enumValue);
    }

    public long stateTick() {
        return stateTick;
    }

    public void setStateTick(long stateTick) {
        this.stateTick = stateTick;
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

    public boolean isKilled() {
        return killed;
    }

    public void setKilled(boolean killed) {
        this.killed = killed;
    }

    @Override
    public void reset() {
        enumValue = GhostState.LOCKED;
        killed = false;
        flashing = false;
        threatenedByPac = false;
    }
}
