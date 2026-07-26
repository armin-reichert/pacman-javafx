/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component;

import static java.util.Objects.requireNonNull;

public class Elroy implements EntityComponent {

    public enum Boost { NONE, MEDIUM, LARGE }

    private boolean enabled;
    private Boost boost;

    @Override
    public void reset() {
        enabled = false;
        boost = Boost.NONE;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Boost boost() {
        return boost;
    }

    public void setBoost(Boost speed) {
        this.boost = requireNonNull(speed);
    }

    @Override
    public String toString() {
        return "Elroy{" +
            "enabled=" + enabled +
            ", boost=" + boost +
            '}';
    }
}
