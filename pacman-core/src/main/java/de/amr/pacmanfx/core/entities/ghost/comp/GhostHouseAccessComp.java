/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class GhostHouseAccessComp implements GameEntityComp {

    private boolean leftHouse;

    private boolean reachedHouseEntry;

    private boolean reachedRevivalPosition;

    public boolean leftHouse() {
        return leftHouse;
    }

    public void setLeftHouse(boolean leftHouse) {
        this.leftHouse = leftHouse;
    }

    public boolean reachedHouseEntry() {
        return reachedHouseEntry;
    }

    public void setReachedHouseEntry(boolean reachedHouseEntry) {
        this.reachedHouseEntry = reachedHouseEntry;
    }

    public boolean reachedRevivalPosition() {
        return reachedRevivalPosition;
    }

    public void setReachedRevivalPosition(boolean reachedRevivalPosition) {
        this.reachedRevivalPosition = reachedRevivalPosition;
    }

    @Override
    public void reset() {
        leftHouse = false;
        reachedHouseEntry = false;
        reachedRevivalPosition = false;
    }
}
