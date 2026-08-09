/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.messageview.comp;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.messageview.system.MessageViewAnimationSystem;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class MessageView3DAnimationComp implements GameEntityComponent, Disposable {

    private final AnimationRegistry registry;

    public static double hiddenZPosition(MessageView messageView) {
        final MessageView3DComp view3D = messageView.requireComp(MessageView3DComp.class);
        return 0.5 * view3D.root().getBoundsInLocal().getHeight();
    }

    private static class MoveInOutAnimation extends ManagedAnimation {

        private final MessageView messageView;

        public MoveInOutAnimation(MessageView messageView) {
            super("Level Message Movement");
            this.messageView = requireNonNull(messageView);
            setAnimationFactory(this::createAnimationFX);
        }

        private Animation createAnimationFX() {
            final MessageView3DComp view3D = messageView.requireComp(MessageView3DComp.class);

            double hiddenZ = hiddenZPosition(messageView);
            double visibleZ = -(hiddenZ + 2);

            var moveUp = new TranslateTransition(Duration.seconds(1), view3D.root());
            moveUp.setToZ(visibleZ);

            var moveDown = new TranslateTransition(Duration.seconds(1), view3D.root());
            moveDown.setToZ(hiddenZ);

            var movement = new SequentialTransition(
                moveUp,
                new PauseTransition(Duration.seconds(view3D.displaySeconds())),
                moveDown
            );
            movement.setOnFinished(_ -> MessageViewAnimationSystem.hideMessageView(messageView));

            return movement;
        }
    }

    public MessageView3DAnimationComp(AnimationRegistry registry, MessageView messageView) {
        this.registry = registry;

        final var moveInOutAnimation = new MoveInOutAnimation(messageView);
        registry.register(MessageView3DAnimationID.MESSAGE_MOVING, moveInOutAnimation);
    }

    public AnimationRegistry registry() {
        return registry;
    }

    @Override
    public void dispose() {
        registry.optAnimation(MessageView3DAnimationID.MESSAGE_MOVING).ifPresent(ManagedAnimation::dispose);
    }

    @Override
    public void reset() {

    }
}
