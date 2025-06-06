/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public abstract class SpriteAnimationMap implements ActorAnimationMap {

    private final SpriteSheet spriteSheet;
    private final Map<String, SpriteAnimation> animationsByID = new HashMap<>();
    protected String currentAnimationID;

    protected SpriteAnimationMap(SpriteSheet spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    public SpriteSheet spriteSheet() { return spriteSheet; }

    //TODO this is somewhat crude but currently the way to keep e.g. the sprites up-to-date with an actors' direction etc.
    protected abstract void updateActorSprites(Actor actor);

    public void set(String key, SpriteAnimation animation) {
        animationsByID.put(key, animation);
    }

    public SpriteAnimation animation(String id) {
        return animationsByID.get(id);
    }

    public String currentAnimationID() { return currentAnimationID; }

    public boolean isCurrentAnimationID(String id) {
        requireNonNull(id);
        return id.equals(currentAnimationID);
    }

    public final RectArea currentSprite(Actor actor) {
        var currentAnimation = currentAnimation();
        if (currentAnimation == null) {
            return null;
        }
        updateActorSprites(actor);
        return currentAnimation.currentSprite();
    }

    public SpriteAnimation currentAnimation() {
        return currentAnimationID != null ? animation(currentAnimationID) : null;
    }

    @Override
    public String selectedAnimationID() {
        return currentAnimationID;
    }

    @Override
    public void selectAnimationAtFrame(String id, int frameIndex) {
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
    public void play() {
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