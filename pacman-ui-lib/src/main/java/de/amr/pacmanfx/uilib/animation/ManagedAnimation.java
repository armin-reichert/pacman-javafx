package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public abstract class ManagedAnimation {

    public static final boolean CONTINUE = false, FROM_START = true;

    protected final AnimationManager animationManager;
    protected final String description;
    protected Animation animation;

    protected ManagedAnimation(AnimationManager animationManager, String description) {
        this.animationManager = requireNonNull(animationManager);
        this.description = requireNonNull(description);
    }

    protected abstract Animation createAnimation();

    public Optional<Animation> animation() {
        return Optional.ofNullable(animation);
    }

    public Animation getOrCreateAnimation() {
        if (animation == null) {
            animation = createAnimation();
            animationManager.register(description, animation);
        }
        return animation;
    }

    public void invalidate() {
        animation = null;
    }

    public void play(boolean playMode) {
        getOrCreateAnimation();
        if (animation.getStatus() != Animation.Status.RUNNING) {
            if (playMode == FROM_START) {
                Logger.info("Playing animation {} from start", description);
                animation.playFromStart();
            } else if (playMode == CONTINUE) {
                Logger.info("Continuing animation {}", description);
                animation.play();
            }
        }
    }

    public void stop() {
        if (animation != null && animation.getStatus() != Animation.Status.STOPPED) {
            Logger.info("Stopping animation {}", description);
            animation.stop();
        }
    }
}