/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

/**
 * @author Armin Reichert
 */
public interface GameClock {

    void setTargetFrameRate(int fps);
}
