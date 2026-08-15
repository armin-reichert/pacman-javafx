/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.levelCounter.system;

import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterBehavior;

import static java.util.Objects.requireNonNull;

public class LevelCounterSystem {

    public LevelCounterSystem() {}

    public void setCounterBehavior(LevelCounter levelCounter, LevelCounterBehavior behavior) {
        requireNonNull(levelCounter);
        requireNonNull(behavior);
        levelCounter.data().setBehavior(behavior);
    }
    public void clearCounter(LevelCounter levelCounter) {
        levelCounter.data().symbolCodes().clear();
    }

    public boolean isCounterFull(LevelCounter levelCounter) {
        final var data = levelCounter.data();
        return data.symbolCodes().size() == data.capacity();
    }

    public void updateCounter(LevelCounter levelCounter, int levelNumber, int symbolCode) {
        requireNonNull(levelCounter);

        final var data = levelCounter.data();
        if (levelNumber == 1) {
            data.symbolCodes().clear();
            data.setEnabled(true);
        }
        if (!data.isEnabled() || data.capacity() == 0) {
            return;
        }

        data.symbolCodes().add(symbolCode);
        if (isCounterFull(levelCounter)) {
            switch (data.behavior()) {
                case DISABLE_WHEN_FULL -> data.setEnabled(false);
                case SHIFT_WHEN_FULL   -> data.symbolCodes().removeFirst();
            }
        }
    }

    public void enableCounter(LevelCounter levelCounter, boolean enabled) {
        levelCounter.data().setEnabled(enabled);
    }

    public void setCounterCapacity(LevelCounter levelCounter, int capacity) {
        levelCounter.data().setCapacity(capacity);
    }
}
