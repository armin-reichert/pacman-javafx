/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Camera;
import javafx.scene.Node;

public interface CameraControlledView {
    DoubleProperty viewPortWidthProperty();
    DoubleProperty viewPortHeightProperty();
    Node viewPort();
    Camera camera();
}