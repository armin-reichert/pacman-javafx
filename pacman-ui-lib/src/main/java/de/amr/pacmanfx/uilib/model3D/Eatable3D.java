/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.Vector2i;
import javafx.scene.Node;

/**
 * Implemented by eatable 3D shapes.
 */
public interface Eatable3D extends Disposable {
    Node node();
    default Vector2i tile() { return (Vector2i) node().getProperties().get("tile"); }
    default void setTile(Vector2i tile) { node().getProperties().put("tile", tile); }
    void onEaten();
}