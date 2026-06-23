/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.viewmodel;

import de.amr.pacmanfx.ui.config.world.Maze3DSettings;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class Maze3DSettingsVM {

    public final FloatProperty obstacleBaseHeightProperty;

    public final FloatProperty obstacleCornerRadiusProperty;

    public final FloatProperty obstacleOpacityProperty;

    public final FloatProperty obstacleWallThicknessProperty;

    public final ObjectProperty<Color> darkWallFillColorProperty;

    public Maze3DSettingsVM() {
        obstacleBaseHeightProperty = new SimpleFloatProperty();
        obstacleCornerRadiusProperty = new SimpleFloatProperty();
        obstacleOpacityProperty = new SimpleFloatProperty();
        obstacleWallThicknessProperty = new SimpleFloatProperty();
        darkWallFillColorProperty = new SimpleObjectProperty<>();
    }

    public void init(Maze3DSettings settings) {
        obstacleBaseHeightProperty.set(settings.obstacleBaseHeight());
        obstacleCornerRadiusProperty.set(settings.obstacleCornerRadius());
        obstacleOpacityProperty.set(settings.obstacleOpacity());
        obstacleWallThicknessProperty.set(settings.obstacleWallThickness());
        darkWallFillColorProperty.set(Color.valueOf(settings.darkWallFillColor()));
    }
}
