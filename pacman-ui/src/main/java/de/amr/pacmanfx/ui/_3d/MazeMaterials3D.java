/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.UfxColors;
import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_FLOOR_COLOR;
import static de.amr.pacmanfx.uilib.Ufx.*;

public record MazeMaterials3D(PhongMaterial floor, PhongMaterial wallBase, PhongMaterial wallTop) implements Disposable {

    public static MazeMaterials3D create(WorldMapColorScheme colorScheme, DoubleProperty wallOpacity) {
        final PhongMaterial floorMaterial = colorBoundPhongMaterial(PROPERTY_3D_FLOOR_COLOR);
        floorMaterial.setSpecularPower(128);

        final PhongMaterial wallBaseMaterial = colorBoundPhongMaterial(wallOpacity.map(
            opacity -> UfxColors.colorWithOpacity(Color.valueOf(colorScheme.wallStroke()), opacity.doubleValue())));
        wallBaseMaterial.setSpecularPower(64);

        final PhongMaterial wallTopMaterial = coloredPhongMaterial(Color.valueOf(colorScheme.wallFill()));

        return new MazeMaterials3D(floorMaterial, wallBaseMaterial, wallTopMaterial);
    }

    @Override
    public void dispose() {
        floor.diffuseColorProperty().unbind();
        floor.specularColorProperty().unbind();
        wallBase.diffuseColorProperty().unbind();
        wallBase.specularColorProperty().unbind();
        wallTop.diffuseColorProperty().unbind();
        wallTop.specularColorProperty().unbind();
    }
}
