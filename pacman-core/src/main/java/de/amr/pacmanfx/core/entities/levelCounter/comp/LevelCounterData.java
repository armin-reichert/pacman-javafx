/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.levelCounter.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

import java.util.ArrayList;
import java.util.List;

public class LevelCounterData implements GameEntityComp {

    private final List<Integer> symbolCodes = new ArrayList<>();

    private LevelCounterBehavior behavior;

    private boolean enabled;

    private int capacity;

    public LevelCounterData() {}

    @Override
    public void reset() {
        symbolCodes.clear();
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LevelCounterBehavior behavior() {
        return behavior;
    }

    public void setBehavior(LevelCounterBehavior behavior) {
        this.behavior = behavior;
    }

    public List<Integer> symbolCodes() {
        return symbolCodes;
    }
}
