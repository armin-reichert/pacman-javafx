/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.ui2d.GameContext;
import javafx.animation.Animation;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public interface AnimatedPac3D {

    Node node();

    PointLight light();

    void init(GameContext context);

    void update(GameContext context);

    Animation createDyingAnimation(GameContext context);

    void walk();

    void stand();

    void setPower(boolean power);

    Property<DrawMode> drawModeProperty();

    Property<Boolean> lightedProperty();
}