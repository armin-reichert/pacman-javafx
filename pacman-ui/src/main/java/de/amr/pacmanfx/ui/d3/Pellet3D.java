/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.math.Vector2i;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class Pellet3D extends MeshView {

    private Vector2i tile;

    public Pellet3D(Mesh mesh) {
        super(mesh);
    }

    public void setLocation(Vector2i tile, double z) {
        this.tile = requireNonNull(tile);
        setTranslateX(tile.x() * TS + HTS);
        setTranslateY(tile.y() * TS + HTS);
        setTranslateZ(z);
    }

    public Vector2i tile() {
        return tile;
    }
}
