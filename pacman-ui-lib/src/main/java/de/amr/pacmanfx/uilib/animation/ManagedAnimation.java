package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;

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
        if (playMode == FROM_START) {
            animation.playFromStart();
        } else if (playMode == CONTINUE) {
            animation.play();
        }
    }

    public void stop() {
        if (animation != null) {
            animation.stop();
        }
    }
}