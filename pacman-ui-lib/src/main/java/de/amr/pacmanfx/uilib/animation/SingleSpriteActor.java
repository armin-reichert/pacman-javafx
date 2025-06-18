/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.AnimatedActor;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class SingleSpriteActor extends Actor implements AnimatedActor {

    private final ActorAnimationMap animationMap;

    public SingleSpriteActor(Sprite sprite) {
        requireNonNull(sprite);
        animationMap = new SingleSpriteWithoutAnimation(sprite);
    }

    @Override
    public Optional<ActorAnimationMap> animations() {
        return Optional.of(animationMap);
    }
}
