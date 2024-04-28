/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

/**
 * @author Armin Reichert
 */
public interface GameClock {

    double getActualFrameRate();

    double getTargetFrameRate();

    void setTargetFrameRate(double fps);

    void start();

    void stop();

    boolean isRunning();

    boolean isPaused();
}
