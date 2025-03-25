/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.level;

import javafx.animation.Animation;

/**
 * @author Armin Reichert
 */
public interface Pac3D {

    PacShape3D shape3D();

    void init();

    void update();

    Animation createDyingAnimation();

    void setPowerMode(boolean power);
}