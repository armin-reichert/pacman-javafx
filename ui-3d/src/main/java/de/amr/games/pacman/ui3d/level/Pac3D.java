/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.ui2d.GameContext;
import javafx.animation.Animation;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public interface Pac3D {

    Node root();

    void init();

    void update(GameContext context);

    Animation createDyingAnimation();

    void setPower(boolean power);

    Property<DrawMode> drawModeProperty();

    PointLight light();
}