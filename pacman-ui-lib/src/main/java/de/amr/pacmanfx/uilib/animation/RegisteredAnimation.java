/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Disposable;
import javafx.animation.Animation;
import org.tinylog.Logger;
import org.tinylog.Supplier;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A lazily created JavaFX animation that is registered in an {@link AnimationRegistry}.
 * <p>
 * A {@code RegisteredAnimation} encapsulates:
 * <ul>
 *   <li>a unique label identifying the animation</li>
 *   <li>a factory that creates the underlying JavaFX {@link Animation}</li>
 *   <li>lifecycle management (play, pause, stop, dispose)</li>
 *   <li>automatic registration in the associated {@link AnimationRegistry}</li>
 * </ul>
 * <p>
 * The embedded JavaFX animation is created on demand via {@link #getOrCreateAnimationFX()}.
 * This allows animations to be registered early without incurring construction cost until needed.
 */
public class RegisteredAnimation implements Disposable {

    /** Human‑readable identifier for logging and debugging. */
    private final String label;

    /** Registry that owns and manages this animation. */
    private final AnimationRegistry registry;

    /** Factory for creating the JavaFX animation lazily. */
    private Supplier<Animation> factory;

    /** The lazily created JavaFX animation instance. */
    protected Animation animationFX;

    /**
     * Creates a registered animation without an initial factory.
     * A factory must be provided later via {@link #setFactory(Supplier)}.
     *
     * @param registry the animation registry
     * @param label    unique label for this animation
     */
    public RegisteredAnimation(AnimationRegistry registry, String label) {
        this.registry = requireNonNull(registry);
        this.label = requireNonNull(label);
        registry.register(this);
    }

    /**
     * Creates a registered animation with a factory for lazy instantiation.
     *
     * @param registry the animation registry
     * @param label    unique label for this animation
     * @param factory  factory that creates the JavaFX animation
     */
    public RegisteredAnimation(AnimationRegistry registry, String label, Supplier<Animation> factory) {
        this.registry = requireNonNull(registry);
        this.label = requireNonNull(label);
        this.factory = requireNonNull(factory);
        registry.register(this);
    }

    /**
     * Sets or replaces the factory used to create the JavaFX animation.
     *
     * @param factory the animation factory
     */
    public void setFactory(Supplier<Animation> factory) {
        this.factory = requireNonNull(factory);
    }

    /** @return the label identifying this animation */
    public String label() {
        return label;
    }

    /**
     * @return the underlying JavaFX animation, if already created
     */
    public Optional<Animation> animationFX() {
        return Optional.ofNullable(animationFX);
    }

    /**
     * Returns the JavaFX animation, creating it if necessary.
     * <p>
     * If creation fails or the factory returns {@code null}, an exception is thrown.
     *
     * @return the JavaFX animation instance
     */
    public Animation getOrCreateAnimationFX() {
        if (animationFX == null) {
            try {
                animationFX = factory.get();
            } catch (Exception x) {
                Logger.error("Creating JavaFX animation '{}' failed", label);
                throw new IllegalStateException("Animation creation failed", x);
            }
            if (animationFX == null) {
                Logger.error("Creating JavaFX animation '{}' returned null", label);
                throw new IllegalStateException("Animation factory returned null");
            }
        }
        return animationFX;
    }

    /**
     * Hook for subclasses to release additional resources during disposal.
     * Called after the animation instance has been cleared.
     */
    protected void freeResources() {}

    /**
     * Disposes this animation:
     * <ul>
     *   <li>stops the animation</li>
     *   <li>clears listeners and references</li>
     *   <li>invokes {@link #freeResources()}</li>
     *   <li>marks this animation for disposal in the registry</li>
     * </ul>
     */
    @Override
    public final void dispose() {
        stop();
        if (animationFX != null) {
            animationFX.setOnFinished(null);
            animationFX = null;
            freeResources();
        }
        registry.markForDisposal(this);
        Logger.info("Disposed animation '{}'", label);
    }

    /**
     * Invalidates the cached JavaFX animation so it will be recreated on next use.
     */
    public void invalidate() {
        animationFX = null;
    }

    /**
     * Plays the animation from the beginning, creating it if necessary.
     * Does nothing if the animation is already running.
     */
    public void playFromStart() {
        Animation animationFX = getOrCreateAnimationFX();
        requireNonNull(animationFX);
        if (animationFX.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Play animation '{}' from start", label);
            animationFX.playFromStart();
        }
    }

    /**
     * Plays the animation if it is not already running.
     * If the animation has been paused, it continues from the paused position.
     */
    public void playOrContinue() {
        Animation animation = getOrCreateAnimationFX();
        requireNonNull(animation);
        if (animation.getStatus() != Animation.Status.RUNNING) {
            Logger.trace("Continue/play animation '{}'", label);
            animation.play();
        }
    }

    /**
     * Pauses the animation if it is currently running.
     * Logs a warning if the animation cannot be paused (e.g., embedded animations).
     */
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

    /**
     * Stops the animation if it is currently running.
     * Logs a warning if the animation cannot be stopped.
     */
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

    /**
     * @return {@code true} if the animation exists and is currently running
     */
    public boolean isRunning() {
        return animationFX != null && animationFX.getStatus() == Animation.Status.RUNNING;
    }
}
