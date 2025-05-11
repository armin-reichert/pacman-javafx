/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import java.util.List;

public interface LevelCounter {
    void clear();
    void update(int levelNumber, byte symbol);
    void setEnabled(boolean enabled);
    boolean isEnabled();
    List<Byte> symbols();
}
