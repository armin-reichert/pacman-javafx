/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3d.messageview.comp;

import de.amr.basics.Disposable;
import de.amr.basics.ecs.GameEntityComp;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;

public class MessageView3DAnimationComp implements GameEntityComp, Disposable {

    private final AnimationRegistry registry;
    private final ManagedAnimation moveInOut;

    public static double hiddenZPosition(MessageView3DComp view3D) {
        return 0.5 * view3D.root().getBoundsInLocal().getHeight();
    }

    public MessageView3DAnimationComp(AnimationRegistry registry, MessageView3DComp view3D) {
        this.registry = registry;
        moveInOut = new MoveInOutAnimation(view3D);
        registry.register(MessageView3DAnimationID.MESSAGE_MOVING, moveInOut);
    }

    public ManagedAnimation moveInOut() {
        return moveInOut;
    }

    @Override
    public void dispose() {
        if (moveInOut != null) {
            moveInOut.dispose();
            registry.unregister(MessageView3DAnimationID.MESSAGE_MOVING);
        }
    }
}
