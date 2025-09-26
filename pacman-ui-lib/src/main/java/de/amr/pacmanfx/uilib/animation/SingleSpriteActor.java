/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.AnimationSupport;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class SingleSpriteActor extends Actor implements AnimationSupport {

    private final SingleSpriteWithoutAnimation animationManager;

    public SingleSpriteActor(RectShort sprite) {
        requireNonNull(sprite);
        animationManager = new SingleSpriteWithoutAnimation(sprite);
    }

    @Override
    public Optional<AnimationManager> animationManager() {
        return Optional.of(animationManager);
    }
}
