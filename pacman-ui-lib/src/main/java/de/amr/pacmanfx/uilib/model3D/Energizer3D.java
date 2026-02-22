/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2i;
import javafx.scene.Node;

/**
 * 3D energizer pellet.
 */
public interface Energizer3D extends Disposable {
    Node shape();
    Vector2i tile();
    void startPumping();
    void stopPumping();
    void onEaten();
    void hide();
}