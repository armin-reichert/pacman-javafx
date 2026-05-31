/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d2;

import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;

public class SpriteAnimationManager {

    private final SpriteAnimationSet animationSet;
    private final SpriteAnimationTimer animationTimer;

    public SpriteAnimationManager() {
        animationSet = new SpriteAnimationSet();
        animationTimer = new SpriteAnimationTimer(animationSet);
    }

    public SpriteAnimationSet animationSet() {
        return animationSet;
    }

    public void startAnimationTimer() {
        animationTimer.start();
    }

    public void stopAnimationTimer() {
        animationTimer.stop();
    }
}
