/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.basics.spriteanim.SpriteAnimationSet;
import javafx.animation.AnimationTimer;

import java.util.Objects;

public class SpriteAnimationTimer extends AnimationTimer {

    private SpriteAnimationSet spriteAnimationSet;

    public SpriteAnimationTimer(SpriteAnimationSet spriteAnimationSet) {
        this.spriteAnimationSet = Objects.requireNonNull(spriteAnimationSet);
    }

    @Override
    public void handle(long now) {
        if (spriteAnimationSet != null) {
            spriteAnimationSet.updateAnimations(now);
        }
    }

    public SpriteAnimationSet spriteAnimationSet() {
        return spriteAnimationSet;
    }

    public void setSpriteAnimationSet(SpriteAnimationSet spriteAnimationSet) {
        this.spriteAnimationSet = spriteAnimationSet;
    }
}
