package de.amr.games.pacman.ui2d.scene.common;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;

public interface ScrollableGameScene2D {
    DoubleProperty availableWidthProperty();
    DoubleProperty availableHeightProperty();
    Canvas canvas();
    Parent root();
}
