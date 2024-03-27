/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import de.amr.games.pacman.model.GameLevel;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public abstract class Steering {

    public static final Steering NONE = new Steering() {
        @Override
        public void steer(GameLevel level) {}
    };

    private boolean enabled;

    public void init() {}

    public abstract void steer(GameLevel level);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        Logger.trace("Steering ({})) {}", getClass().getSimpleName(), enabled ? "enabled" : "disabled");
    }
}