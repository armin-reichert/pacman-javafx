/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.math.Vector2i;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;

import static java.util.Objects.requireNonNull;

public class Pellet3D extends MeshView {

    private final Vector2i tile;

    public Pellet3D(Mesh mesh, Vector2i tile) {
        super(mesh);
        this.tile = requireNonNull(tile);
    }

    public Vector2i tile() {
        return tile;
    }
}
