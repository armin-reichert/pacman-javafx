package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public abstract class ManagedAnimation {

    public static final boolean CONTINUE = false, FROM_START = true;

    protected AnimationManager animationManager;
    protected final String identifier;
    protected Animation animation;

    protected abstract Animation createAnimation();

    protected ManagedAnimation(AnimationManager animationManager, String identifier) {
        this.animationManager = requireNonNull(animationManager);
        this.identifier = requireNonNull(identifier);
    }

    public String identifier() {
        return identifier;
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
        }
        animationManager = null;
    }

    public void invalidate() {
        animation = null;
    }

    public void play(boolean playMode) {
        getOrCreateAnimation();
        if (animation.getStatus() != Animation.Status.RUNNING) {
            if (playMode == FROM_START) {
                Logger.trace("Playing animation {} from start", identifier);
                animation.playFromStart();
            } else if (playMode == CONTINUE) {
                Logger.trace("Continuing animation {}", identifier);
                animation.play();
            }
        }
    }

    public void stop() {
        if (animation != null && animation.getStatus() != Animation.Status.STOPPED) {
            Logger.trace("Stopping animation {}", identifier);
            animation.stop();
        }
    }
}