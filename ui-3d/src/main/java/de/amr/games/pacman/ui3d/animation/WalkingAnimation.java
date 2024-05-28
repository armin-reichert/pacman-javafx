/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.animation;

import javafx.animation.Animation;

/**
 * @author Armin Reichert
 */
public interface WalkingAnimation {

    void play();

    void stop();

    void setPowerWalking(boolean power);

    Animation animation();
}