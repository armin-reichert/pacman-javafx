/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.model.GameLevel;
import javafx.animation.Animation;
import javafx.scene.LightBase;
import javafx.scene.effect.Light;

/**
 * @author Armin Reichert
 */
public interface Pac3D {

    PacShape3D shape3D();

    LightBase light();

    void init();

    void update(GameLevel level);

    Animation createDyingAnimation();

    void setPowerMode(boolean power);
}