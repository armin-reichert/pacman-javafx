/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.uilib.animation.SpriteAnimation;
import de.amr.games.pacman.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class SpriteAnimationSet implements Animations {

    protected final Map<String, SpriteAnimation> animationsByID = new HashMap<>();
    protected String currentAnimationID;

    public void add(Map<String, SpriteAnimation> entries) {
        animationsByID.putAll(entries);
    }

    public SpriteAnimation animation(String id) {
        return animationsByID.get(id);
    }

    public boolean isCurrentAnimationID(String id) {
        requireNonNull(id);
        return id.equals(currentAnimationID);
    }

    public final RectArea currentSprite(Actor actor) {
        var currentAnimation = currentAnimation();
        if (currentAnimation == null) {
            return null;
        }
        RectArea[] newSelection = selectedSprites(currentAnimation().spriteSheet(), actor);
        if (newSelection != null) {
            currentAnimation.setSprites(newSelection);
        }
        return currentAnimation.currentSprite();
    }

    protected RectArea[] selectedSprites(SpriteSheet spriteSheet, Actor actor) {
        return null;
    }

    @Override
    public String currentID() {
        return currentAnimationID;
    }

    public SpriteAnimation currentAnimation() {
        return currentAnimationID != null ? animation(currentAnimationID) : null;
    }

    @Override
    public void select(String id, int frameIndex) {
        if (!id.equals(currentAnimationID)) {
            currentAnimationID = id;
            if (currentAnimation() != null) {
                currentAnimation().setFrameIndex(0);
            } else {
                Logger.warn("No animation with ID {} exists", id);
            }
        }
    }

    @Override
    public void start() {
        if (currentAnimation() != null) {
            currentAnimation().play();
        }
    }

    @Override
    public void stop() {
        if (currentAnimation() != null) {
            currentAnimation().stop();
        }
    }

    @Override
    public void reset() {
        if (currentAnimation() != null) {
            currentAnimation().reset();
        }
    }
}