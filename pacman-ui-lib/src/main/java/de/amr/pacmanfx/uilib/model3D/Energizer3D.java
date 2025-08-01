/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.scene.Node;

/**
 * 3D energizer pellet.
 */
public interface Energizer3D extends Disposable {
    Node shape();
    Vector2i tile();
    void startPumping();
    void stopPumping();
    void setEatenAnimation(ManagedAnimation animation);
    void onEaten();
    void hide();
}