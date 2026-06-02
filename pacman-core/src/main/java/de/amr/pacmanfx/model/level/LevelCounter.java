/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.level;

import java.util.List;

public interface LevelCounter {
    List<Integer> symbolCodes();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    void clear();
    void update(int levelNumber, int symbolCode);
}