/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

import static java.util.Objects.requireNonNull;

public class GhostStateComp implements GameEntityComp {

    private GhostState enumValue;

    private long stateTick;

    private boolean pacPowerFading;

    private boolean pacPower;

    private int killChainIndex;

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

    public boolean isPacPowerFading() {
        return pacPowerFading;
    }

    public void setPacPowerFading(boolean pacPowerFading) {
        this.pacPowerFading = pacPowerFading;
    }

    public boolean hasPacPower() {
        return pacPower;
    }

    public void setPacPower(boolean pacPower) {
        this.pacPower = pacPower;
    }

    public int killChainIndex() {
        return killChainIndex;
    }

    public void setKillChainIndex(int killChainIndex) {
        this.killChainIndex = killChainIndex;
    }

    @Override
    public void reset() {
        stateTick = 0;
        enumValue = GhostState.LOCKED;
        killChainIndex = -1;
        pacPowerFading = false;
        pacPower = false;
    }
}
