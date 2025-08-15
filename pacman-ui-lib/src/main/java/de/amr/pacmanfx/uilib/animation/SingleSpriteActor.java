/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Animated;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class SingleSpriteActor extends Actor implements Animated {

    private final SingleSpriteWithoutAnimation singleSpriteMap;

    public SingleSpriteActor(RectShort sprite) {
        requireNonNull(sprite);
        singleSpriteMap = new SingleSpriteWithoutAnimation(sprite);
    }

    @Override
    public Optional<AnimationManager> animations() {
        return Optional.of(singleSpriteMap);
    }
}
