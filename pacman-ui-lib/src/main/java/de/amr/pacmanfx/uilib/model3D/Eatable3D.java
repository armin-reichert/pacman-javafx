/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Vector2i;
import javafx.geometry.Point3D;
import javafx.scene.Node;

/**
 * Implemented by eatable 3D shapes.
 */
public interface Eatable3D {
    Node shape3D();
    void onEaten();
    default Point3D position() {
        return new Point3D(shape3D().getTranslateX(), shape3D().getTranslateY(), shape3D().getTranslateZ());
    }
    default Vector2i tile() { return (Vector2i) shape3D().getProperties().get("tile"); }
    default void setTile(Vector2i tile) { shape3D().getProperties().put("tile", tile); }
}