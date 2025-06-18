/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationMap implements ActorAnimationMap {

    protected final SpriteSheet<?> spriteSheet;
    protected final Map<String, SpriteAnimation> animationsByID = new HashMap<>();
    protected String currentAnimationID;

    public SpriteAnimationMap(SpriteSheet<?> spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    public SpriteSheet<?> spriteSheet() { return spriteSheet; }

    // TODO: this is somewhat crude but currently the way to keep the sprites up-to-date with actor direction etc.
    protected void updateActorSprites(Actor actor) {}

    public String currentAnimationID() { return currentAnimationID; }

    public boolean isCurrentAnimationID(String id) {
        requireNonNull(id);
        return id.equals(currentAnimationID);
    }

    public Sprite currentSprite(Actor actor) {
        var currentAnimation = currentAnimation();
        if (currentAnimation == null) {
            return null;
        }
        updateActorSprites(actor);
        return (Sprite) currentAnimation.currentSprite();
    }

    @Override
    public SpriteAnimation animation(String id) {
        if (!animationsByID.containsKey(id)) {
            SpriteAnimation spriteAnimation = createAnimation(id);
            animationsByID.put(id, spriteAnimation);
        }
        return animationsByID.get(id);
    }

    protected SpriteAnimation createAnimation(String id) {
        throw new UnsupportedOperationException("No idea how to create animation with ID " + id);
    }

    public void setAnimation(String id, SpriteAnimation animation) {
        requireNonNull(id);
        requireNonNull(animation);
        animationsByID.put(id, animation);
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