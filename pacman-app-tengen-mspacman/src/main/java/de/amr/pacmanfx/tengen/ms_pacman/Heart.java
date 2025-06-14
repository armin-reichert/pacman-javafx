package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.AnimatedActor;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import java.util.Optional;

public class Heart extends Actor implements AnimatedActor {

    private final SpriteAnimationMap animations;

    public Heart(TengenMsPacMan_SpriteSheet spriteSheet) {
        animations = new SpriteAnimationMap(spriteSheet);
        animations.set("heart", SpriteAnimation.createAnimation().ofSprite(spriteSheet.sprite(SpriteID.HEART)).end());
        animations.selectAnimation("heart");
    }

    @Override
    public Optional<ActorAnimationMap> animations() {
        return Optional.of(animations);
    }
}
