/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.levelCounter.system;


import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterData;

public class LevelCounterSystem {

    public static void clear(LevelCounter levelCounter) {
        levelCounter.requireComponent(LevelCounterData.class).symbolCodes().clear();
    }

    public static boolean isFull(LevelCounter levelCounter) {
        final var data = levelCounter.requireComponent(LevelCounterData.class);
        return data.symbolCodes().size() == data.capacity();
    }

    public static void update(LevelCounter levelCounter, int levelNumber, int symbolCode) {
        final var data = levelCounter.requireComponent(LevelCounterData.class);
        if (levelNumber == 1) {
            data.symbolCodes().clear();
            data.setEnabled(true);
        }
        if (data.isEnabled()) {
            if (data.symbolCodes().size() == data.capacity()) {
                data.symbolCodes().removeFirst();
            }
            data.symbolCodes().add(symbolCode);
        }
    }

    public static void enable(LevelCounter levelCounter, boolean enabled) {
        levelCounter.requireComponent(LevelCounterData.class).setEnabled(enabled);
    }

    public static void setCapacity(LevelCounter levelCounter, int capacity) {
        levelCounter.requireComponent(LevelCounterData.class).setCapacity(capacity);
    }
}
