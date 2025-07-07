package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.uilib.model3D.Destroyable;
import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public abstract class ManagedAnimation implements Destroyable {

    public static final boolean CONTINUE = false, FROM_START = true;

    protected AnimationManager animationManager;
    protected String label;
    protected Animation animation;

    protected abstract Animation createAnimation();

    protected ManagedAnimation(AnimationManager animationManager, String label) {
        this.animationManager = requireNonNull(animationManager);
        this.label = requireNonNull(label);
        animationManager.register(label, this);
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

    @Override
    public void destroy() {
        animationManager.stopAnimation(this); // handles "embedded animation cannot be stopped" issue!
        if (animation != null) {
            animation.setOnFinished(null);
            animation = null;
            Logger.info("Destroyed managed animation with label '{}'", label);
        }
        animationManager = null;
    }

    public void invalidate() {
        animation = null;
    }

    public void playFromStart() {
        play(FROM_START);
    }

    public void playOrContinue() {
        play(CONTINUE);
    }

    protected void play(boolean playMode) {
        animationManager.playAnimation(this, playMode);
    }

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