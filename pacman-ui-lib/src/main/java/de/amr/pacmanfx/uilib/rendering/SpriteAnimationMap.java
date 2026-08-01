/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.Naming;
import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A sprite animation container implementing the sprite animation accessor facade.
 *
 * @param <ID> Sprite animation ID type
 */
public abstract class SpriteAnimationMap<ID extends Naming> implements SpriteAnimationAccess {

    protected final SpriteSheet<ID> spriteSheet;
    protected final Map<Naming, SpriteAnimation> animationsByID = new HashMap<>();
    protected Naming selectedAnimationID;
    protected Function<Naming, SpriteAnimation> factory;

    public SpriteAnimationMap(SpriteSheet<ID> spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    public void setFactory(Function<Naming, SpriteAnimation> factory) {
        this.factory = factory;
    }

    public SpriteSheet<ID> spriteSheet() { return spriteSheet; }

    public boolean isSelected(Naming id) {
        requireNonNull(id);
        return id.equals(selectedAnimationID);
    }

    @Override
    public void select(Naming animationID) {
        selectedAnimationID = animationID;
    }

    @Override
    public RectShort currentSprite() {
        final SpriteAnimation currentAnimation = currentAnimation();
        return currentAnimation == null ? null : currentAnimation.sprite();
    }

    @Override
    public SpriteAnimation animation(Naming animationID) {
        if (!animationsByID.containsKey(animationID)) {
            SpriteAnimation spriteAnimation = factory.apply(animationID);
            animationsByID.put(animationID, spriteAnimation);
        }
        return animationsByID.get(animationID);
    }

    public void setAnimation(Naming animationID, SpriteAnimation animation) {
        requireNonNull(animationID);
        requireNonNull(animation);
        animationsByID.put(animationID, animation);
    }

    public SpriteAnimation currentAnimation() {
        return selectedAnimationID != null ? animation(selectedAnimationID) : null;
    }

    @Override
    public Naming selectedAnimationID() {
        return selectedAnimationID;
    }

    @Override
    public void setAnimationFrame(Naming animationID, int frameIndex) {
        if (!animationID.equals(selectedAnimationID)) {
            selectedAnimationID = animationID;
            if (currentAnimation() != null) {
                currentAnimation().setFrame(0);
            } else {
                Logger.warn("No animation with ID {} exists", animationID);
            }
        }
    }

    @Override
    public int currentFrame() {
        return currentAnimation() != null ? currentAnimation().frame() : -1;
    }

    @Override
    public int numFrames() {
        return currentAnimation() != null ? currentAnimation().numFrames() : 0;
    }

    @Override
    public void playSelected() {
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