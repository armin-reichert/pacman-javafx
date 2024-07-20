/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.ui2d.GameContext;
import javafx.animation.Animation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public interface Pac3D {

    Node node();

    void init(GameContext context);

    void updateAlive(GameContext context);

    Animation createDyingAnimation(GameContext context);

    void setPower(boolean power);

    Property<DrawMode> drawModeProperty();

    BooleanProperty lightOnProperty();

    DoubleProperty lightRangeProperty();
}