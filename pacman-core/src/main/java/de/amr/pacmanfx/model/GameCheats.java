/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.level.GameLevel;
import javafx.beans.property.BooleanProperty;

public interface GameCheats {

    /** @return property indicating whether a cheat has been used */
    BooleanProperty cheatUsedProperty();

    /** @return property indicating whether Pac‑Man is immune to death */
    BooleanProperty immuneProperty();

    /** @return {@code true} if Pac‑Man is currently immune */
    boolean isImmune();

    /** @return {@code true} if autopilot is currently active */
    boolean isUsingAutopilot();

    /** @return property indicating whether autopilot mode is active */
    BooleanProperty usingAutopilotProperty();

    void clearCheats();

    void updateCheats(GameLevel level);
}
