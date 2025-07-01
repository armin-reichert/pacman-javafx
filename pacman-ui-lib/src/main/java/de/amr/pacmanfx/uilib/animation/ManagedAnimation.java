package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public abstract class ManagedAnimation {

    public static final boolean CONTINUE = false, FROM_START = true;

    protected AnimationManager animationManager;
    protected String id;
    protected String label;
    protected Animation animation;

    protected abstract Animation createAnimation();

    protected ManagedAnimation(AnimationManager animationManager, String label) {
        this.animationManager = requireNonNull(animationManager);
        this.label = requireNonNull(label);
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public Optional<Animation> animation() {
        return Optional.ofNullable(animation);
    }

    public Animation getOrCreateAnimation() {
        if (animation == null) {
            animation = createAnimation();
        }
        return animation;
    }

    public void destroy() {
        animationManager.stopAnimation(this); // handles "embedded animation cannot be stopped" issue!
        if (animation != null) {
            animation.setOnFinished(null);
            animation = null;
            Logger.info("Destroyed managed animation with ID '{}'", id);
        }
        animationManager = null;
    }

    public void invalidate() {
        animation = null;
    }

    public void play(boolean playMode) {
        getOrCreateAnimation();
        animationManager.register(label, this);
        if (animation.getStatus() != Animation.Status.RUNNING) {
            if (playMode == FROM_START) {
                Logger.trace("Playing animation {} from start", id);
                animation.playFromStart();
            } else if (playMode == CONTINUE) {
                Logger.trace("Continuing animation {}", id);
                animation.play();
            }
        }
    }

    public void stop() {
        if (animation != null && animation.getStatus() != Animation.Status.STOPPED) {
            Logger.trace("Stopping animation {}", id);
            animation.stop();
        }
    }
}