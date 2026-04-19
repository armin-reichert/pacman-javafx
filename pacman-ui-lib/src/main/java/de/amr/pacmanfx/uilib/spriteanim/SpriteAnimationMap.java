/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.spriteanim;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.AnimationSet;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public abstract class SpriteAnimationMap<SID extends Enum<SID>> implements AnimationSet {

    protected final SpriteSheet<SID> spriteSheet;
    protected final Map<Object, SpriteAnimation> animationsByID = new HashMap<>();
    protected SID selectedID;

    public SpriteAnimationMap(SpriteSheet<SID> spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    protected abstract SpriteAnimation createAnimation(Object animationID);

    public SpriteSheet<SID> spriteSheet() { return spriteSheet; }

    public boolean isSelected(Object id) {
        requireNonNull(id);
        return id.equals(selectedID);
    }

    @Override
    public RectShort currentSprite() {
        final SpriteAnimation currentAnimation = currentAnimation();
        return currentAnimation == null ? null : currentAnimation.currentSprite();
    }

    @Override
    public SpriteAnimation animation(Object animationID) {
        if (!animationsByID.containsKey(animationID)) {
            SpriteAnimation spriteAnimation = createAnimation(animationID);
            animationsByID.put(animationID, spriteAnimation);
        }
        return animationsByID.get(animationID);
    }

    public void setAnimation(Object animationID, SpriteAnimation animation) {
        requireNonNull(animationID);
        requireNonNull(animation);
        animationsByID.put(animationID, animation);
    }

    public SpriteAnimation currentAnimation() {
        return selectedID != null ? animation(selectedID) : null;
    }

    @Override
    public Object selectedAnimationID() {
        return selectedID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setAnimationFrame(Object animationID, int frameIndex) {
        if (!animationID.equals(selectedID)) {
            selectedID = (SID) animationID;
            if (currentAnimation() != null) {
                currentAnimation().setCurrentFrameIndex(0);
            } else {
                Logger.warn("No animation with ID {} exists", animationID);
            }
        }
    }

    @Override
    public int currentFrame() {
        return currentAnimation() != null ? currentAnimation().currentFrame() : -1;
    }

    @Override
    public void playSelectedAnimation() {
        if (currentAnimation() != null) {
            currentAnimation().start();
        }
    }

    @Override
    public void stopSelectedAnimation() {
        if (currentAnimation() != null) {
            currentAnimation().stop();
        }
    }

    @Override
    public void resetSelectedAnimation() {
        if (currentAnimation() != null) {
            currentAnimation().reset();
        }
    }
}