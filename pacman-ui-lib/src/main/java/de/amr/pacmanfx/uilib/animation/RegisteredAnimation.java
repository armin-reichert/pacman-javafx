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

/**
 * A wrapper around a JavaFX animation. Such an animation is registered in its associated animation repository and
 * the embedded JavaFX animation is created on demand.
 */
public abstract class RegisteredAnimation implements Disposable {

    private final String label;
    private final AnimationRegistry registry;
    protected Animation animationFX;

    protected abstract Animation createAnimationFX();

    protected RegisteredAnimation(AnimationRegistry registry, String label) {
        this.registry = requireNonNull(registry);
        this.label = requireNonNull(label);
        registry.register(this);
    }

    public String label() {
        return label;
    }

    public Optional<Animation> animationFX() {
        return Optional.ofNullable(animationFX);
    }

    public Animation getOrCreateAnimationFX() {
        if (animationFX == null) {
            try {
                animationFX = createAnimationFX();
            } catch (Exception x) {
                Logger.error("Creating JavaFX animation '{}' failed", label);
                throw new IllegalStateException(x);
            }
            if (animationFX == null) {
                Logger.error("Creating JavaFX animation '{}' returned null", label);
                throw new IllegalStateException();
            }
        }
        return animationFX;
    }

    /**
     * Called by {@link #dispose()}.
     */
    protected void freeResources() {}

    @Override
    public final void dispose() {
        stop();
        if (animationFX != null) {
            animationFX.setOnFinished(null);
            animationFX = null;
            freeResources();
        }
        registry.markDisposed(this);
        Logger.info("Disposed animation '{}'", label);
    }

    public void invalidate() {
        animationFX = null;
    }

    public void playFromStart() {
        Animation animationFX = getOrCreateAnimationFX();
       requireNonNull(animationFX);
        if (animationFX.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Play animation '{}' from start", label);
            animationFX.playFromStart();
        }
    }

    public void playOrContinue() {
        Animation animation = getOrCreateAnimationFX();
        requireNonNull(animation);
        if (animation.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Continue/play animation '{}'", label);
            animation.play();
        }
    }

    public void pause() {
        if (animationFX != null) {
            try {
                if (animationFX.getStatus() != Animation.Status.PAUSED) {
                    animationFX.pause();
                    Logger.debug("Paused animation '{}'", label);
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not pause (embedded?) animation '{}'", label);
            }
        }
    }

    public void stop() {
        if (animationFX != null) {
            try {
                if (animationFX.getStatus() != Animation.Status.STOPPED) {
                    Logger.debug("Stop animation '{}'", label);
                    animationFX.stop();
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not stop (embedded?) animation '{}'", label);
            }
        }
    }

    public boolean isRunning() {
        return animationFX != null && animationFX.getStatus() == Animation.Status.RUNNING;
    }
}