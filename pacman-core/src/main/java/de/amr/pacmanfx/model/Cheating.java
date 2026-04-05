/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import javafx.beans.property.BooleanProperty;

public interface Cheating {

    /** @return property indicating whether a cheat has been used */
    BooleanProperty cheatUsedProperty();

    /** @return property indicating whether Pac‑Man is immune to death */
    BooleanProperty immuneProperty();

    /** @return property indicating whether autopilot mode is active */
    BooleanProperty usingAutopilotProperty();

    /** Marks that a cheat has been used in this game session. */
    default void raiseFlag() {
        cheatUsedProperty().set(true);
    }

    /** Clears the cheat‑used flag. */
    default void clearFlag() {
        cheatUsedProperty().set(false);
    }

    /** @return {@code true} if Pac‑Man is currently immune */
    default boolean isImmune() {
        return immuneProperty().get();
    }

    /** @return {@code true} if autopilot is currently active */
    default boolean isUsingAutopilot() {
        return usingAutopilotProperty().get();
    }
}
