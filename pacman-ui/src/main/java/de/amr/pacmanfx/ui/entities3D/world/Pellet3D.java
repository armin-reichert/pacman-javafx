/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.world;

import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.assets.DisposableGraphicsObject;
import javafx.scene.shape.Shape3D;

import static de.amr.basics.TileDimension.HTS;
import static de.amr.basics.TileDimension.TS;
import static java.util.Objects.requireNonNull;

public class Pellet3D implements DisposableGraphicsObject {

    private Shape3D shape;
    private Vector2i tile;

    public Pellet3D(Shape3D shape) {
        this.shape = requireNonNull(shape);
        setLocation(Vector2i.ZERO, -HTS);
    }

    @Override
    public void dispose() {
        if (shape != null) {
            cleanupShape3D(shape);
            shape = null;
        }
    }

    public Shape3D root() {
        return shape;
    }

    public void setLocation(Vector2i tile, double z) {
        this.tile = requireNonNull(tile);
        shape.setTranslateX(tile.x() * TS + HTS);
        shape.setTranslateY(tile.y() * TS + HTS);
        shape.setTranslateZ(z);
    }

    public Vector2i tile() {
        return tile;
    }
}
