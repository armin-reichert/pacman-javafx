/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.level;

import de.amr.games.pacman.lib.Vector2i;
import javafx.geometry.Point3D;
import javafx.scene.Node;

/**
 * @author Armin Reichert
 */
public interface Eatable3D {
    Node shape3D();
    void onEaten();
    Point3D position();
    default Vector2i tile() { return (Vector2i) shape3D().getProperties().get("tile"); }
    default void setTile(Vector2i tile) { shape3D().getProperties().put("tile", tile); }
}