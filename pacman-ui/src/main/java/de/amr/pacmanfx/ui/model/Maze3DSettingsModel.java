/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.model;

import de.amr.pacmanfx.ui.config.world.Maze3DSettings;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

public class Maze3DSettingsModel {

    public final ObjectProperty<Color> floorColorProperty;

    public final ObjectProperty<Color> lightColorProperty;

    public final DoubleProperty wallHeightProperty;

    public final DoubleProperty wallOpacityProperty;

    public final FloatProperty obstacleBaseHeightProperty;

    public final FloatProperty obstacleCornerRadiusProperty;

    public final FloatProperty obstacleOpacityProperty;

    public final FloatProperty obstacleWallThicknessProperty;

    public final ObjectProperty<Color> darkWallFillColorProperty;

    public Maze3DSettingsModel() {
        floorColorProperty = new SimpleObjectProperty<>();
        lightColorProperty = new SimpleObjectProperty<>();
        wallHeightProperty = new SimpleDoubleProperty();
        wallOpacityProperty = new SimpleDoubleProperty();
        obstacleBaseHeightProperty = new SimpleFloatProperty();
        obstacleCornerRadiusProperty = new SimpleFloatProperty();
        obstacleOpacityProperty = new SimpleFloatProperty();
        obstacleWallThicknessProperty = new SimpleFloatProperty();
        darkWallFillColorProperty = new SimpleObjectProperty<>();
    }

    public void init(Maze3DSettings settings) {
        floorColorProperty.set(settings.floorColor());
        lightColorProperty.set(settings.lightColor());
        wallHeightProperty.set(settings.wallHeight());
        wallOpacityProperty.set(settings.wallOpacity());
        obstacleBaseHeightProperty.set(settings.obstacleBaseHeight());
        obstacleCornerRadiusProperty.set(settings.obstacleCornerRadius());
        obstacleOpacityProperty.set(settings.obstacleOpacity());
        obstacleWallThicknessProperty.set(settings.obstacleWallThickness());
        darkWallFillColorProperty.set(Color.valueOf(settings.darkWallFillColor()));
    }
}
