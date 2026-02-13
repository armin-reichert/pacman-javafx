/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

// Translate: top-left corner (without padding) at origin, surface top at z=0
public class MazeFloor3D extends Box implements Disposable {

    public MazeFloor3D(PhongMaterial floorMaterial, float width, float height, float thickness, float padding) {
        requireNonNull(floorMaterial);
        setWidth(width);
        setHeight(height);
        setDepth(thickness);
        setMaterial(floorMaterial);
        setTranslateX(0.5 * width);
        setTranslateY(0.5 * height);
        setTranslateZ(0.5 * thickness);

        //TODO rethink this. It complicates collision handling
        getTransforms().add(new Translate(-padding, 0, 0));
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
