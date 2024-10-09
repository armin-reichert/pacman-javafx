/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.AnimatedEntity;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class SpriteAnimationCollection implements Animations {

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

    public boolean currently(String name) {
        checkNotNull(name);
        return name.equals(currentAnimationName);
    }

    public final RectArea currentSprite(AnimatedEntity animatedEntity) {
        var currentAnimation = current();
        if (currentAnimation == null) {
            return null;
        }
        RectArea[] newSelection = selectedSprites(current().spriteSheet(), animatedEntity.entity());
        if (newSelection != null) {
            currentAnimation.setSprites(newSelection);
        }
        return currentAnimation.currentSprite();
    }

    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Entity entity) {
        return null;
    }

    @Override
    public SpriteAnimation current() {
        return currentAnimationName != null ? animation(currentAnimationName) : null;
    }

    @Override
    public void select(String name, int index) {
        if (!name.equals(currentAnimationName)) {
            currentAnimationName = name;
            if (current() != null) {
                current().setFrameIndex(0);
            } else {
                Logger.warn("No animation with name {} exists", name);
            }
        }
    }

    @Override
    public void startSelected() {
        if (current() != null) {
            current().start();
        }
    }

    @Override
    public void stopSelected() {
        if (current() != null) {
            current().stop();
        }
    }

    @Override
    public void resetSelected() {
        if (current() != null) {
            current().reset();
        }
    }
}