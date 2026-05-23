package de.amr.pacmanfx.uilib.animation;

import de.amr.basics.spriteanim.SpriteAnimationSet;
import javafx.animation.AnimationTimer;

public class SpriteAnimationTimer extends AnimationTimer {

    private SpriteAnimationSet spriteAnimationSet;

    @Override
    public void handle(long now) {
        if (spriteAnimationSet != null) {
            spriteAnimationSet.updateAnimations(now);
        }
    }

    public void setSpriteAnimationSet(SpriteAnimationSet spriteAnimationSet) {
        this.spriteAnimationSet = spriteAnimationSet;
    }
}
