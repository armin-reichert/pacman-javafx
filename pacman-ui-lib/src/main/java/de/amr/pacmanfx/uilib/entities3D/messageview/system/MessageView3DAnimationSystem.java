/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.messageview.system;

import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DComp;

import static java.util.Objects.requireNonNull;

public class MessageView3DAnimationSystem {

    public static void showMessageViewCenteredAt(MessageView messageView, double centerX, double centerY) {
        requireNonNull(messageView);

        // Place message view at hidden position
        final MessageView3DComp view3D = messageView.reqComp(MessageView3DComp.class);
        view3D.root().setVisible(true);
        view3D.root().setTranslateX(centerX - 0.5 * view3D.imageView().getFitWidth());
        view3D.root().setTranslateY(centerY);
        view3D.root().setTranslateZ(MessageView3DAnimationComp.hiddenZPosition(view3D));

        final MessageView3DAnimationComp anim3D = messageView.reqComp(MessageView3DAnimationComp.class);
        anim3D.moveInOut().playFromStart();
    }

    public static void hideMessageView(MessageView messageView) {
        if (messageView != null) {
            messageView.hide();
            messageView.reqComp(MessageView3DComp.class).root().setVisible(false);
        }
    }
}
