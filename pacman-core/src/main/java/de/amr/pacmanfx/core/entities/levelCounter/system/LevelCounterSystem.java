/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.levelCounter.system;

import de.amr.pacmanfx.core.entities.LevelCounter;

public class LevelCounterSystem {

    public void clear(LevelCounter levelCounter) {
        levelCounter.data().symbolCodes().clear();
    }

    public boolean isFull(LevelCounter levelCounter) {
        final var data = levelCounter.data();
        return data.symbolCodes().size() == data.capacity();
    }

    public void update(LevelCounter levelCounter, int levelNumber, int symbolCode) {
        final var data = levelCounter.data();
        if (levelNumber == 1) {
            data.symbolCodes().clear();
            data.setEnabled(true);
        }
        if (data.isEnabled() && data.capacity() > 0) {
            if (data.symbolCodes().size() == data.capacity()) {
                data.symbolCodes().removeFirst();
            }
            data.symbolCodes().add(symbolCode);
        }
    }

    public void enable(LevelCounter levelCounter, boolean enabled) {
        levelCounter.data().setEnabled(enabled);
    }

    public void setCapacity(LevelCounter levelCounter, int capacity) {
        levelCounter.data().setCapacity(capacity);
    }
}
