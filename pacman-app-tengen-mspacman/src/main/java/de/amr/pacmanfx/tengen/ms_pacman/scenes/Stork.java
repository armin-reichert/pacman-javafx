/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import java.util.Optional;

import static de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID.STORK;

public class Stork extends Actor {

    public static final String ANIM_FLYING = "flying";

    private final SpriteAnimationManager<SpriteID> spriteAnimations;
    private boolean bagReleasedFromBeak;

    public Stork(TengenMsPacMan_SpriteSheet spriteSheet) {
        spriteAnimations = new SpriteAnimationManager<>(spriteSheet);
        spriteAnimations.setAnimation(ANIM_FLYING,
            SpriteAnimation.build().of(spriteSheet.content().spriteSequence(STORK)).frameTicks(8).forever());
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }

    @Override
    public Optional<AnimationManager> animations() {
        return Optional.of(spriteAnimations);
    }
}