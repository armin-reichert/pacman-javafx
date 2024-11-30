/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.sound.GameSound;
import javafx.animation.Animation;

/**
 * @author Armin Reichert
 */
public interface Pac3D {

    PacShape3D shape3D();

    void init();

    void update(GameContext context);

    Animation createDyingAnimation(GameSound sounds);

    void setPowerMode(boolean power);
}