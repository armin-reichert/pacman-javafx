/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class MazeFloor3D extends Box implements Disposable {

    public MazeFloor3D(PreferencesManager prefs, GameLevel level, PhongMaterial floorMaterial) {
        requireNonNull(prefs);
        requireNonNull(level);
        requireNonNull(floorMaterial);

        final Vector2i worldSizePx = level.worldMap().terrainLayer().sizeInPixel();
        final float padding = prefs.getFloat("3d.floor.padding");
        final float thickness = prefs.getFloat("3d.floor.thickness");
        final float sizeX = worldSizePx.x() + 2 * padding;
        final float sizeY = worldSizePx.y();

        setWidth(sizeX);
        setHeight(sizeY);
        setDepth(thickness);
        setMaterial(floorMaterial);

        // Translate: top-left corner (without padding) at origin, surface top at z=0
        final var translate = new Translate(0.5 * sizeX - padding, 0.5 * sizeY, 0.5 * thickness);
        getTransforms().add(translate);
    }

    @Override
    public void dispose() {
        translateXProperty().unbind();
        translateYProperty().unbind();
        translateZProperty().unbind();
        materialProperty().unbind();
        setMaterial(null);
        Logger.info("Unbound and cleared 3D floor");
    }
}
