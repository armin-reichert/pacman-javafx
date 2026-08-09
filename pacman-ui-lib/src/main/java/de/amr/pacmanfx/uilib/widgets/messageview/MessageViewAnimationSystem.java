/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets.messageview;


import de.amr.pacmanfx.uilib.animation.ManagedAnimation;

import static java.util.Objects.requireNonNull;

public class MessageViewAnimationSystem {

    public static void showMessageViewCenteredAt(MessageViewAnimations animations, MessageView messageView, double centerX, double centerY) {
        requireNonNull(messageView);
        requireNonNull(animations);

        // Place message view at hidden position
        messageView.setTranslateX(centerX - 0.5 * messageView.imageView().getFitWidth());
        messageView.setTranslateY(centerY);
        messageView.setTranslateZ(MessageViewAnimations.hiddenZPosition(messageView));

        // Play move in/out animation
        animations.registry().optAnimation(MessageViewAnimations.AnimationID.MESSAGE_MOVING)
            .ifPresent(ManagedAnimation::playFromStart);
    }
}
