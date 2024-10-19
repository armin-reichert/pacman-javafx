package de.amr.games.pacman.ui2d.scene.common;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Camera;
import javafx.scene.Node;

public interface ScrollableGameScene {
    DoubleProperty scrollAreaWidthProperty();
    DoubleProperty scrollAreaHeightProperty();
    Node scrollArea();
    Camera camera();
}
