/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.vm;

import de.amr.pacmanfx.ui.settings.world.Maze3DSettings;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

public class Maze3DSettingsVM {

    public final ObjectProperty<Color> floorColorProperty;

    public final ObjectProperty<Color> lightColorProperty;

    public final DoubleProperty wallHeightProperty;

    public final DoubleProperty wallOpacityProperty;

    public final FloatProperty obstacleWallThicknessProperty;

    public final ObjectProperty<Color> darkWallFillColorProperty;

    public Maze3DSettingsVM() {
        floorColorProperty = new SimpleObjectProperty<>();
        lightColorProperty = new SimpleObjectProperty<>();
        wallHeightProperty = new SimpleDoubleProperty();
        wallOpacityProperty = new SimpleDoubleProperty();
        obstacleWallThicknessProperty = new SimpleFloatProperty();
        darkWallFillColorProperty = new SimpleObjectProperty<>();
    }

    public void init(Maze3DSettings settings) {
        floorColorProperty.set(settings.floorColor());
        lightColorProperty.set(settings.lightColor());
        wallHeightProperty.set(settings.wallHeight());
        wallOpacityProperty.set(settings.wallOpacity());
        obstacleWallThicknessProperty.set(settings.obstacleWallThickness());
        darkWallFillColorProperty.set(Color.valueOf(settings.darkWallFillColor()));
    }
}
