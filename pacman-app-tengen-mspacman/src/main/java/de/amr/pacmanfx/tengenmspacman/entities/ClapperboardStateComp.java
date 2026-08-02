/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities;


import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class ClapperboardStateComp implements GameEntityComponent {

    private ClapperboardState state;
    private boolean textVisible;
    private int tick;
    private boolean running;

    @Override
    public void reset() {
    }

    public ClapperboardState state() {
        return state;
    }

    public void setState(ClapperboardState state) {
        this.state = state;
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
