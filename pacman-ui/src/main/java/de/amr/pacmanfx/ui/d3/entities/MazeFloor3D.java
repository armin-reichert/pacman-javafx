/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.entities;

import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

// Translate: top-left corner (without padding) at origin, surface top at z=0
public class MazeFloor3D extends Group implements DisposableGraphicsObject {

    private final Box plane;

    public MazeFloor3D(PhongMaterial floorMaterial, float width, float height, float thickness) {
        requireNonNull(floorMaterial);

        plane = new Box(width, height, thickness);
        plane.setMaterial(floorMaterial);

        getChildren().add(plane);
    }

    public Box plane() {
        return plane;
    }

    /** @return the Z-coordinate of the top surface of the floor */
    public double top() {
        return plane.getTranslateZ() - 0.5 * plane.getDepth();
    }

    @Override
    public void dispose() {
        cleanupShape3D(plane);
        Logger.info("Unbound and cleared 3D floor");
    }
}
