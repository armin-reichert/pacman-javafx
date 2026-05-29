/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Pac;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class GameCheats {

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);
    private final BooleanProperty immune = new SimpleBooleanProperty(false);
    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);

    public GameCheats() {}

    /** @return property indicating whether a cheat has been used */
    public BooleanProperty cheatUsedProperty() {
        return cheatUsed;
    }

    /** @return property indicating whether Pac‑Man is immune to death */
    public BooleanProperty immuneProperty() {
        return immune;
    }

    /** @return {@code true} if Pac‑Man is currently immune */
    public boolean isImmune() {
        return immuneProperty().get();
    }

    /** @return {@code true} if autopilot is currently active */
    public boolean isUsingAutopilot() {
        return usingAutopilotProperty().get();
    }

    /** @return property indicating whether autopilot mode is active */
    public BooleanProperty usingAutopilotProperty() {
        return usingAutopilot;
    }

    public void clear() {
        cheatUsed.set(false);
        immune.set(false);
        usingAutopilot.set(false);
    }

    public void update(GameLevel level) {
        if (level.isDemoLevel() || !level.game().isPlayingLevel()) {
            return;
        }
        final Pac pac = level.entities().pac();
        pac.immuneProperty().set(isImmune());
        pac.usingAutopilotProperty().set(isUsingAutopilot());
        if (isImmune() || isUsingAutopilot()) {
            cheatUsed.set(true);
        }
    }
}
