/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.world;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.scene.shape.Shape3D;

import static java.util.Objects.requireNonNull;

public class Pellet3D implements DisposableGraphicsObject {

    private Shape3D shape;
    private Vector2i tile;

    public Pellet3D(Shape3D shape) {
        this.shape = requireNonNull(shape);
        setLocation(Vector2i.ZERO, -WorldMap.HTS);
    }

    @Override
    public void dispose() {
        if (shape != null) {
            cleanupShape3D(shape);
            shape = null;
        }
    }

    public Shape3D shape() {
        return shape;
    }

    public void setLocation(Vector2i tile, double z) {
        this.tile = requireNonNull(tile);
        shape.setTranslateX(tile.x() * WorldMap.TS + WorldMap.HTS);
        shape.setTranslateY(tile.y() * WorldMap.TS + WorldMap.HTS);
        shape.setTranslateZ(z);
    }

    public Vector2i tile() {
        return tile;
    }
}
