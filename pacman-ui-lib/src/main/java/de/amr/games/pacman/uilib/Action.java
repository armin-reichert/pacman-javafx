/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

/**
 * @author Armin Reichert
 */
public interface Action {

    void execute();

    default boolean isEnabled() { return true; }
}