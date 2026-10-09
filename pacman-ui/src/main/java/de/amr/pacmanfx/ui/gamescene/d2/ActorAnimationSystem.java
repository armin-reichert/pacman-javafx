/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.engine.GameVariantRenderConfig;
import de.amr.pacmanfx.engine.GameVariantRuntime;

//TODO make individual animation systems for ghosts and Pac-Man?
public class ActorAnimationSystem {

    public static void ensureActorAnimationsCreated(GameVariantRuntime runtime, GameLevel level) {
        final GameVariantRenderConfig renderConfig = runtime.uiConfig().renderConfig();
        final SpriteAnimationContainer animationContainer = runtime.spriteAnimContainer();
        final ActorSpriteAnimController animController = runtime.playConfig().systems().actorSpriteAnimController();

        final Pac pac = level.entitySet().pac();
        if (animController.hasNoAnimations(pac)) {
            animController.setAnimations(pac, renderConfig.createPacAnimations(animationContainer));
            resetPacAnimation(animController, pac);
        }

        level.entitySet().ghosts().forEach(ghost -> {
            if (animController.hasNoAnimations(ghost)) {
                animController.setAnimations(ghost,
                    renderConfig.createGhostAnimations(animationContainer, ghost.personality()));
                resetGhostAnimation(animController, ghost);
            }
        });
    }

    // Called from game event handler
    public static void resetActorAnimations(ActorSpriteAnimController animController, GameLevel level) {
        resetPacAnimation(animController, level.entitySet().pac());
        level.entitySet().ghosts().forEach(ghost -> resetGhostAnimation(animController, ghost));
    }

    public static void resetPacAnimation(ActorSpriteAnimController animController, Pac pac) {
        animController.select(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animController.resetSelected(pac);
    }

    public static void resetGhostAnimation(ActorSpriteAnimController animController, Ghost ghost) {
        animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
        animController.resetSelected(ghost);
    }
}
