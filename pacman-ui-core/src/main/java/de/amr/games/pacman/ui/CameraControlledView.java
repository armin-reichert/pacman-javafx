/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Camera;
import javafx.scene.Node;

public interface CameraControlledView {
    DoubleProperty viewPortWidthProperty();
    DoubleProperty viewPortHeightProperty();
    Node viewPort();
    Camera camera();
}