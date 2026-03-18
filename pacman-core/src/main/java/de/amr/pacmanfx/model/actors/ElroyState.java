/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class ElroyState {

    public enum Mode {ZERO, ONE, TWO}

    private boolean enabled;
    private Mode mode;

    public void reset() {
        enabled = false;
        mode = Mode.ZERO;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        Logger.info("Cruise Elroy speed increase is: {}, active: {}", this.mode, this.enabled);
    }

    public Mode mode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = requireNonNull(mode);
        Logger.info("Cruise Elroy is: {}, active: {}", this.mode, this.enabled);
    }
}
