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

    private final String label;
    private final AnimationRegistry animationRegistry;
    protected Animation animation;

    protected abstract Animation createAnimation();

    protected ManagedAnimation(AnimationRegistry animationRegistry, String label) {
        this.animationRegistry = requireNonNull(animationRegistry);
        this.label = requireNonNull(label);
        animationRegistry.register(this);
    }

    public AnimationRegistry animationRegistry() {
        return animationRegistry;
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

    /**
     * Called by {@link #dispose()}.
     */
    protected void freeResources() {}

    @Override
    public final void dispose() {
        stop();
        if (animation != null) {
            freeResources();
            animation.setOnFinished(null);
            animation = null;
            Logger.info("Disposed animation '{}'", label);
        }
    }

    public void invalidate() {
        animation = null;
    }

    public void playFromStart() {
        Animation animation = getOrCreateAnimation();
        requireNonNull(animation);
        if (animation.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Play animation '{}' from start", label);
            animation.playFromStart();
        }
    }

    public void playOrContinue() {
        Animation animation = getOrCreateAnimation();
        requireNonNull(animation);
        if (animation.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Continue/play animation '{}'", label);
            animation.play();
        }
    }

    public void pause() {
        if (animation != null) {
            try {
                if (animation.getStatus() != Animation.Status.PAUSED) {
                    animation.pause();
                    Logger.debug("Paused animation '{}'", label);
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not pause (embedded?) animation '{}'", label);
            }
        }
    }

    public void stop() {
        if (animation != null) {
            try {
                if (animation.getStatus() != Animation.Status.STOPPED) {
                    Logger.debug("Stop animation '{}'", label);
                    animation.stop();
                }
            } catch (IllegalStateException x) {
                Logger.warn("Could not stop (embedded?) animation '{}'", label);
            }
        }
    }

    public boolean isRunning() {
        return animation != null && animation.getStatus() == Animation.Status.RUNNING;
    }
}