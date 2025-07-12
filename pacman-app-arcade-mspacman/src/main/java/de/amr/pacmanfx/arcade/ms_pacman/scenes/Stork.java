/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.Animated;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import java.util.Optional;

class Stork extends Actor implements Animated {
    private final SpriteAnimationMap<SpriteID> animationMap;

    public Stork(ArcadeMsPacMan_SpriteSheet spriteSheet) {
        super(null); // no game context
        animationMap = new SpriteAnimationMap<>(spriteSheet);
        animationMap.setAnimation("flying",
            SpriteAnimation.build().of(spriteSheet.spriteSeq(SpriteID.STORK)).frameTicks(8).forever());
    }

    @Override
    public Optional<ActorAnimationMap> animationMap() {
        return Optional.of(animationMap);
    }
}
