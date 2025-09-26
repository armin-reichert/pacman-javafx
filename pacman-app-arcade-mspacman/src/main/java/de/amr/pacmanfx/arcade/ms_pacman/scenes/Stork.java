/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import java.util.Optional;

class Stork extends Actor {

    public static final String ANIM_FLYING = "flying";

    private final SpriteAnimationManager<SpriteID> animationManager;

    public Stork(ArcadeMsPacMan_SpriteSheet spriteSheet) {
        animationManager = new SpriteAnimationManager<>(spriteSheet);
        animationManager.setAnimation(ANIM_FLYING,
            SpriteAnimation.build().of(spriteSheet.spriteSequence(SpriteID.STORK)).frameTicks(8).forever());
    }

    @Override
    public Optional<AnimationManager> animationManager() {
        return Optional.of(animationManager);
    }
}
