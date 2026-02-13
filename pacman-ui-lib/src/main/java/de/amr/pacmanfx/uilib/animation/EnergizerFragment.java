/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.scene.shape.Shape3D;

public interface EnergizerFragment {

    Shape3D shape();

    void dispose();

    void fly(Vector3f gravity);

    void move();

    void setSize(double size);

    double size();
}
