/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import java.util.List;

public interface LevelCounter {
    List<Byte> symbols();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    void clear();
    void update(int levelNumber, byte symbol);
}