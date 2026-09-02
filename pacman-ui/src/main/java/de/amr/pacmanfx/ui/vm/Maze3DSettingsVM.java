/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.vm;

import de.amr.pacmanfx.ui.settings.world.Maze3DSettings;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

public class Maze3DSettingsVM {

    private final ObjectProperty<Color> floorColor;
    private final ObjectProperty<Color> lightColor;
    private final DoubleProperty wallHeight;
    private final DoubleProperty wallOpacity;
    private final FloatProperty obstacleWallThickness;
    private final ObjectProperty<Color> darkWallFillColor;

    public Maze3DSettingsVM() {
        floorColor = new SimpleObjectProperty<>();
        lightColor = new SimpleObjectProperty<>();
        wallHeight = new SimpleDoubleProperty();
        wallOpacity = new SimpleDoubleProperty();
        obstacleWallThickness = new SimpleFloatProperty();
        darkWallFillColor = new SimpleObjectProperty<>();
    }

    public void init(Maze3DSettings settings) {
        floorColor.set(settings.floorColor());
        lightColor.set(settings.lightColor());
        wallHeight.set(settings.wallHeight());
        wallOpacity.set(settings.wallOpacity());
        obstacleWallThickness.set(settings.obstacleWallThickness());
        darkWallFillColor.set(Color.valueOf(settings.darkWallFillColor()));
    }

    public ObjectProperty<Color> floorColorProperty() {
        return floorColor;
    }

    public ObjectProperty<Color> lightColorProperty() {
        return lightColor;
    }

    public DoubleProperty wallHeightProperty() {
        return wallHeight;
    }

    public DoubleProperty wallOpacityProperty() {
        return wallOpacity;
    }

    public FloatProperty obstacleWallThicknessProperty() {
        return obstacleWallThickness;
    }

    public ObjectProperty<Color> darkWallFillColorProperty() {
        return darkWallFillColor;
    }
}
