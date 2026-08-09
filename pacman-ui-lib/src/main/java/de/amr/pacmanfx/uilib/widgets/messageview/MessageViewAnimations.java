/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets.messageview;

import de.amr.basics.Disposable;
import de.amr.basics.Named;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

public class MessageViewAnimations implements Disposable {

    public enum AnimationID implements Named {
        MESSAGE_MOVING
    }

    private final AnimationRegistry registry;

    public static double hiddenZPosition(MessageView messageView) {
        return 0.5 * messageView.getBoundsInLocal().getHeight();
    }

    private static class MoveInOutAnimation extends ManagedAnimation {

        private final MessageView messageView;

        public MoveInOutAnimation(MessageView messageView) {
            super("Level Message Movement");
            this.messageView = messageView;
            setAnimationFactory(this::createAnimationFX);
        }

        private Animation createAnimationFX() {
            double hiddenZ = hiddenZPosition(messageView);
            double visibleZ = -(hiddenZ + 2);
            var moveUp = new TranslateTransition(Duration.seconds(1), messageView);
            moveUp.setToZ(visibleZ);
            var moveDown = new TranslateTransition(Duration.seconds(1), messageView);
            moveDown.setToZ(hiddenZ);
            var movement = new SequentialTransition(
                moveUp,
                new PauseTransition(Duration.seconds(messageView.displaySeconds())),
                moveDown
            );
            movement.setOnFinished(_ -> messageView.setVisible(false));
            return movement;
        }
    }

    public MessageViewAnimations(AnimationRegistry registry, MessageView messageView) {
        this.registry = registry;

        final var moveInOutAnimation = new MoveInOutAnimation(messageView);
        registry.register(AnimationID.MESSAGE_MOVING, moveInOutAnimation);
    }

    public AnimationRegistry registry() {
        return registry;
    }

    @Override
    public void dispose() {
        registry.optAnimation(AnimationID.MESSAGE_MOVING).ifPresent(ManagedAnimation::dispose);
    }
}
