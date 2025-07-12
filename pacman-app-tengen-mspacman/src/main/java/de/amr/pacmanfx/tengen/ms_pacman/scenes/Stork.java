/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.Animated;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import java.util.Optional;

import static de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID.STORK;

public class Stork extends Actor implements Animated {
    private final SpriteAnimationMap<SpriteID> animationMap;
    private boolean bagReleasedFromBeak;

    public Stork(TengenMsPacMan_SpriteSheet spriteSheet) {
        super(null);
        animationMap = new SpriteAnimationMap<>(spriteSheet);
        animationMap.setAnimation("flying",
            SpriteAnimation.build()
                .of(spriteSheet.spriteSeq(STORK)).frameTicks(8).forever());
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }

    @Override
    public Optional<ActorAnimationMap> animationMap() {
        return Optional.of(animationMap);
    }
}
