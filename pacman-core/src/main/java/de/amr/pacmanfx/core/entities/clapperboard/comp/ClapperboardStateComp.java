/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.clapperboard.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class ClapperboardStateComp implements GameEntityComp {

    private ClapperboardState enumValue;
    private boolean textVisible;
    private int tick;
    private boolean running;

    public ClapperboardState enumValue() {
        return enumValue;
    }

    public void setEnumValue(ClapperboardState enumValue) {
        this.enumValue = enumValue;
    }

    public boolean textVisible() {
        return textVisible;
    }

    public void setTextVisible(boolean textVisible) {
        this.textVisible = textVisible;
    }

    public boolean running() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int tick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }
}
