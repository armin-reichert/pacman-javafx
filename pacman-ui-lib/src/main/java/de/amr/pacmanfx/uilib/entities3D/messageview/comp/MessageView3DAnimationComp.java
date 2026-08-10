/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.messageview.comp;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;

public class MessageView3DAnimationComp implements GameEntityComponent, Disposable {

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

    @Override
    public void reset() {}
}
