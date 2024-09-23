/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Armin Reichert
 */
public abstract class SpriteAnimations implements Animations {

    protected final Map<String, SpriteAnimation> animationsByName = new HashMap<>();
    protected String currentAnimationName;

    public void add(Map<String, SpriteAnimation> entries) {
        animationsByName.putAll(entries);
    }

    public SpriteAnimation animation(String name) {
        return animationsByName.get(name);
    }

    public String currentAnimationName() {
        return currentAnimationName;
    }

    @Override
    public SpriteAnimation currentAnimation() {
        return currentAnimationName != null ? animation(currentAnimationName) : null;
    }

    public abstract RectArea currentSprite(AnimatedEntity entity);

    @Override
    public void select(String name, int index) {
        if (!name.equals(currentAnimationName)) {
            currentAnimationName = name;
            if (currentAnimation() != null) {
                currentAnimation().setFrameIndex(0);
            } else {
                Logger.warn("No animation with name {} exists", name);
            }
        }
    }

    @Override
    public void startSelected() {
        if (currentAnimation() != null) {
            currentAnimation().start();
        }
    }

    @Override
    public void stopSelected() {
        if (currentAnimation() != null) {
            currentAnimation().stop();
        }
    }

    @Override
    public void resetSelected() {
        if (currentAnimation() != null) {
            currentAnimation().reset();
        }
    }
}