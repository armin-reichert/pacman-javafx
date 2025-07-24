/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Disposable;
import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public abstract class ManagedAnimation implements Disposable {

    protected AnimationManager animationManager;
    protected String label;
    protected Animation animation;

    protected abstract Animation createAnimation();

    protected ManagedAnimation(AnimationManager animationManager, String label) {
        this.animationManager = requireNonNull(animationManager);
        this.label = requireNonNull(label);
        animationManager.register(label, this);
    }

    public AnimationManager animationManager() {
        return animationManager;
    }

    public String label() {
        return label;
    }

    public Optional<Animation> animation() {
        return Optional.ofNullable(animation);
    }

    public Animation getOrCreateAnimation() {
        if (animation == null) {
            try {
                animation = createAnimation();
            } catch (Exception x) {
                Logger.error("Creating animation '{}' failed", label);
                throw new IllegalStateException(x);
            }
            if (animation == null) {
                Logger.error("Creating animation '{}' returned null", label);
                throw new IllegalStateException();
            }
        }
        return animation;
    }

    @Override
    public void dispose() {
        stop();
        if (animation != null) {
            animation.setOnFinished(null);
            animation = null;
            Logger.info("Disposed animation '{}'", label);
        }
        animationManager = null;
    }

    public void invalidate() {
        animation = null;
    }

    public void playFromStart() {
        animationManager.playAnimationFromStart(this);
    }

    public void playOrContinue() { animationManager.playAnimation(this); }

    public void pause() {
        animationManager.pauseAnimation(this);
    }

    public void stop() {
        animationManager.stopAnimation(this);
    }

    public boolean isRunning() {
        return animation != null && animation.getStatus() == Animation.Status.RUNNING;
    }
}