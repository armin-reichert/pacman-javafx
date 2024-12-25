/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.assets;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.AnimatedActor2D;
import de.amr.games.pacman.model.actors.Animations;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Armin Reichert
 */
public class SpriteAnimationCollection implements Animations {

    protected final Map<String, SpriteAnimation> animationsByID = new HashMap<>();
    protected String currentAnimationID;

    public void add(Map<String, SpriteAnimation> entries) {
        animationsByID.putAll(entries);
    }

    public SpriteAnimation animation(String id) {
        return animationsByID.get(id);
    }

    public boolean isCurrentAnimationID(String id) {
        Globals.assertNotNull(id);
        return id.equals(currentAnimationID);
    }

    public final RectArea currentSprite(AnimatedActor2D animatedActor2D) {
        var currentAnimation = currentAnimation();
        if (currentAnimation == null) {
            return null;
        }
        RectArea[] newSelection = selectedSprites(currentAnimation().spriteSheet(), animatedActor2D.actor());
        if (newSelection != null) {
            currentAnimation.setSprites(newSelection);
        }
        return currentAnimation.currentSprite();
    }

    //TODO passing spritesheet is not necessary
    protected RectArea[] selectedSprites(GameSpriteSheet spriteSheet, Actor2D actor) {
        return null;
    }

    @Override
    public String currentAnimationID() {
        return currentAnimationID;
    }

    @Override
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
    public void startCurrentAnimation() {
        if (currentAnimation() != null) {
            currentAnimation().start();
        }
    }

    @Override
    public void stopCurrentAnimation() {
        if (currentAnimation() != null) {
            currentAnimation().stop();
        }
    }

    @Override
    public void resetCurrentAnimation() {
        if (currentAnimation() != null) {
            currentAnimation().reset();
        }
    }
}