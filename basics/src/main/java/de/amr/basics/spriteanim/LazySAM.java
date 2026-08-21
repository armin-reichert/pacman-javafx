/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A sprite animation container implementing the sprite animation accessor facade.
 * Sprite animations are created on-demand by the assigned animation factory.
 */
public class LazySAM implements SpriteAnimFacade {

    private Map<Named, SpriteAnimation> animationsByName;

    protected Named selectedName;

    protected Function<Named, SpriteAnimation> factory;

    protected LazySAM() {}

    private void ensureMapCreated() {
        if (animationsByName == null) {
            animationsByName = new HashMap<>();
        }
    }

    public void setFactory(Function<Named, SpriteAnimation> factory) {
        this.factory = requireNonNull(factory);
    }

    public boolean isSelected(Named name) {
        requireNonNull(name);
        return name.equals(selectedName);
    }

    @Override
    public void select(Named name) {
        selectedName = requireNonNull(name);
    }

    @Override
    public RectShort currentSprite() {
        final SpriteAnimation anim = currentAnimation();
        return anim == null ? null : anim.sprite();
    }

    @Override
    public SpriteAnimation animation(Named name) {
        ensureMapCreated();
        if (!animationsByName.containsKey(name)) {
            final SpriteAnimation anim = factory.apply(name);
            if (anim == null) {
                throw new IllegalStateException("Animation with name '%s' could not be created".formatted(name));
            }
            animationsByName.put(name, anim);
        }
        return animationsByName.get(name);
    }

    public void setAnimation(Named animationID, SpriteAnimation animation) {
        requireNonNull(animationID);
        requireNonNull(animation);
        ensureMapCreated();
        animationsByName.put(animationID, animation);
    }

    public SpriteAnimation currentAnimation() {
        return selectedName != null ? animation(selectedName) : null;
    }

    @Override
    public Named selectedAnimationID() {
        return selectedName;
    }

    @Override
    public void setAnimationFrame(Named name, int frameIndex) {
        requireNonNull(name);
        if (!name.equals(selectedName)) {
            selectedName = name;
        }
        final SpriteAnimation anim = currentAnimation();
        if (anim != null) {
            anim.setFrame(frameIndex);
        } else {
            Logger.warn("Cannot set animation to frame {}: no animation with name '{}' exists", frameIndex, name);
        }
    }

    @Override
    public int currentFrame() {
        final SpriteAnimation anim = currentAnimation();
        return anim != null ? anim.frame() : -1;
    }

    @Override
    public int numFrames() {
        final SpriteAnimation anim = currentAnimation();
        return anim != null ? anim.numFrames() : 0;
    }

    @Override
    public void playSelected() {
        final SpriteAnimation anim = currentAnimation();
        if (anim != null) {
            anim.start();
        }
    }

    @Override
    public void stopSelected() {
        final SpriteAnimation anim = currentAnimation();
        if (anim != null) {
            anim.stop();
        }
    }

    @Override
    public void resetSelected() {
        final SpriteAnimation anim = currentAnimation();
        if (anim != null) {
            anim.reset();
        }
    }
}