/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Actor;

import java.util.List;

public abstract class LevelCounter extends Actor {
    public abstract void clear();
    public abstract void update(int levelNumber, byte symbol);
    public abstract void setEnabled(boolean enabled);
    public abstract boolean isEnabled();
    public abstract List<Byte> symbols();
}
