/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import java.util.List;

public interface LevelCounter {
    void clearLevelCounter();
    void updateLevelCounter(int levelNumber, byte symbol);
    void setLevelCounterEnabled(boolean enabled);
    boolean isLevelCounterEnabled();
    List<Byte> levelCounterSymbols();
}
