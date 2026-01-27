/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import java.util.List;

public interface LevelCounter {
    List<Byte> levelCounterSymbols();
    boolean isLevelCounterEnabled();
    void setLevelCounterEnabled(boolean enabled);
    void clearLevelCounter();
    void updateLevelCounter(int levelNumber, byte symbol);
}