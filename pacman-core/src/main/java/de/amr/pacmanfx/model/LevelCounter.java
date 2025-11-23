/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import java.util.List;

public interface LevelCounter {
    List<Byte> symbols();
    boolean enabled();
    void setEnabled(boolean enabled);
    void clear();
    void update(int levelNumber, byte symbol);
}