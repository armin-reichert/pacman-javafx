/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationManager<SID extends Enum<SID>> implements AnimationManager {

    protected final SpriteSheet<SID> spriteSheet;
    protected final Map<String, SpriteAnimation> animationsByID = new HashMap<>();
    protected String selectedID;

    public SpriteAnimationManager(SpriteSheet<SID> spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    public SpriteSheet<SID> spriteSheet() { return spriteSheet; }

    // TODO: this is somewhat crude but currently the way to keep the sprites up-to-date with actor direction etc.
    protected void updateActorSprites(Actor actor) {}

    public String selectedAnimationID() { return selectedID; }

    public boolean isCurrentAnimationID(String id) {
        requireNonNull(id);
        return id.equals(selectedID);
    }

    @Override
    public RectShort currentSprite(Actor actor) {
        var currentAnimation = currentAnimation();
        if (currentAnimation == null) {
            return null;
        }
        updateActorSprites(actor);
        return currentAnimation.currentSprite();
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
        return selectedID != null ? animation(selectedID) : null;
    }

    @Override
    public String selectedID() {
        return selectedID;
    }

    @Override
    public void selectFrame(String id, int frameIndex) {
        if (!id.equals(selectedID)) {
            selectedID = id;
            if (currentAnimation() != null) {
                currentAnimation().setFrameIndex(0);
            } else {
                Logger.warn("No animation with ID {} exists", id);
            }
        }
    }

    @Override
    public int frameIndex() {
        return currentAnimation() != null ? currentAnimation().frameIndex() : -1;
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