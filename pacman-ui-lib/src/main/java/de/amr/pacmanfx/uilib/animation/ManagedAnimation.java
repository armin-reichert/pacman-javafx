/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.basics.Disposable;
import javafx.animation.Animation;
import org.tinylog.Logger;
import org.tinylog.Supplier;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A lazily created JavaFX animation that can be registered in an {@link AnimationRegistry}.
 * <p>
 * A {@code ManagedAnimation} encapsulates:
 * <ul>
 *   <li>a descriptive label for the animation</li>
 *   <li>a factory that creates the underlying JavaFX {@link Animation}</li>
 *   <li>lifecycle management (play, pause, stop, dispose)</li>
 * </ul>
 * <p>
 * The embedded JavaFX animation is created on demand via {@link #delegate()}.
 * This allows animations to be registered early without incurring construction cost until needed.
 */
public class ManagedAnimation implements Disposable {

    private final String name;

    private Supplier<Animation> animationFactory;

    protected Animation delegate;

    /**
     * Creates a registered animation without an initial factory.
     * A factory must be provided later via {@link #setAnimationFactory(Supplier)}.
     *
     * @param name    unique label for this animation
     */
    public ManagedAnimation(String name) {
        this.name = requireNonNull(name);
    }

    /**
     * Creates a registered animation with a factory for lazy instantiation.
     *
     * @param name    unique label for this animation
     * @param animationFactory  factory that creates the JavaFX animation
     */
    public ManagedAnimation(String name, Supplier<Animation> animationFactory) {
        this.name = requireNonNull(name);
        this.animationFactory = requireNonNull(animationFactory);
    }

    /**
     * Sets or replaces the factory used to create the JavaFX animation.
     *
     * @param animationFactory the animation factory
     */
    public void setAnimationFactory(Supplier<Animation> animationFactory) {
        this.animationFactory = requireNonNull(animationFactory);
    }

    /** @return the name of this animation */
    public String name() {
        return name;
    }

    /**
     * @return the wrapped JavaFX animation, if already created
     */
    public Optional<Animation> optDelegate() {
        return Optional.ofNullable(delegate);
    }

    /**
     * Returns the wrapped JavaFX animation, creating it if necessary.
     * <p>
     * If creation fails or the factory returns {@code null}, an exception is thrown.
     *
     * @return the wrapped JavaFX animation instance
     */
    public final Animation delegate() {
        if (delegate == null) {
            createJavaFXAnimation();
        }
        return delegate;
    }

    private void createJavaFXAnimation() {
        if (animationFactory == null) {
            throw new IllegalStateException("Animation factory for animation '%s' is null".formatted(name));
        }
        try {
            delegate = animationFactory.get();
            if (delegate == null) {
                throw new IllegalStateException("Creating JavaFX animation '%s' returned null".formatted(name));
            }
        }
        catch (Exception x) {
            throw new IllegalStateException("Creating JavaFX animation '%s' failed".formatted(name), x);
        }
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
     * </ul>
     */
    @Override
    public final void dispose() {
        if (delegate != null) {
            stop(); // handles case when FX animation is embedded inside sequential or parallel transition!
            delegate.setOnFinished(null);
            delegate = null;
            freeResources();
        }
    }

    /**
     * Invalidates the cached JavaFX animation so it will be recreated on next use.
     */
    public void invalidate() {
        if (delegate != null) {
            delegate.stop();
        }
        delegate = null;
    }

    /**
     * Plays the animation from the beginning, creating it if necessary.
     * Does nothing if the animation is already running.
     */
    public void playFromStart() {
        final Animation animationFX = delegate();
        if (animationFX.getStatus() != Animation.Status.RUNNING) {
            animationFX.playFromStart();
        }
    }

    /**
     * Plays the animation if it is not already running.
     * If the animation has been paused, it continues from the paused position.
     * Does nothing if the animation is already running.
     */
    public void playOrContinue() {
        final Animation animation = delegate();
        if (animation.getStatus() != Animation.Status.RUNNING) {
            animation.play();
        }
    }

    /**
     * Pauses the animation if it is currently running.
     * Logs a warning if the animation cannot be paused (e.g., embedded animations).
     */
    public void pause() {
        if (delegate == null) {
            return;
        }
        try {
            if (delegate.getStatus() != Animation.Status.PAUSED) {
                delegate.pause();
                Logger.debug("Paused animation '{}'", name);
            }
        } catch (IllegalStateException x) {
            // This may happen if attempt is made to pause animation embedded inside other animation
            Logger.warn("Could not pause (embedded?) animation '{}'", name);
        }
    }

    /**
     * Stops the animation if it is currently running.
     * Logs a warning if the animation cannot be stopped.
     */
    public void stop() {
        if (delegate == null) {
            return;
        }
        try {
            if (delegate.getStatus() != Animation.Status.STOPPED) {
                delegate.stop();
            }
        } catch (IllegalStateException x) {
            Logger.warn("Could not stop (embedded?) animation '{}'", name);
        }
    }

    /**
     * @return {@code true} if the animation exists and is currently running
     */
    public boolean isRunning() {
        return delegate != null && delegate.getStatus() == Animation.Status.RUNNING;
    }
}
