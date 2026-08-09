/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.messageview.system;

import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DAnimationID;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DComp;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MoveInOutAnimation;

import static java.util.Objects.requireNonNull;

public class MessageView3DAnimationSystem {

    public static void showMessageViewCenteredAt(MessageView3DAnimationComp animations, MessageView messageView, double centerX, double centerY) {
        requireNonNull(messageView);
        requireNonNull(animations);

        final MessageView3DComp view3D = messageView.requireComp(MessageView3DComp.class);

        // Place message view at hidden position
        view3D.root().setTranslateX(centerX - 0.5 * view3D.imageView().getFitWidth());
        view3D.root().setTranslateY(centerY);
        view3D.root().setTranslateZ(MessageView3DAnimationComp.hiddenZPosition(view3D));

        if (animations.registry().optAnimation(MessageView3DAnimationID.MESSAGE_MOVING).isEmpty()) {
            buildAnimations(animations.registry(), messageView);
        }
        // Play move in/out animation
        view3D.root().setVisible(true); //TODO check this
        animations.registry().optAnimation(MessageView3DAnimationID.MESSAGE_MOVING)
            .ifPresent(ManagedAnimation::playFromStart);
    }

    public static void hideMessageView(MessageView messageView) {
        if (messageView != null) {
            messageView.hide();
            messageView.requireComp(MessageView3DComp.class).root().setVisible(false);
        }
    }

    private static void buildAnimations(AnimationRegistry registry, MessageView messageView) {
        final MessageView3DComp view3D = messageView.requireComp(MessageView3DComp.class);
        final var moveInOutAnimation = new MoveInOutAnimation(view3D);
        registry.register(MessageView3DAnimationID.MESSAGE_MOVING, moveInOutAnimation);
    }
}
