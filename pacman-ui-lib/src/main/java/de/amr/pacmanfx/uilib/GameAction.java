/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib;

/**
 * Common interface for game actions.
 */
public interface GameAction {

    void execute();

    default boolean isEnabled() { return true; }

    default String name() { return toString(); }
}