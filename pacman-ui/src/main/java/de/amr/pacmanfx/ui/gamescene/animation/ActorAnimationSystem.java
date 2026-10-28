/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.animation;

import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationSystem;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameVariantUIConfig;

//TODO make individual animation systems for ghosts and Pac-Man?
public class ActorAnimationSystem {

    public static void ensureActorAnimationsCreated(EngineUIContext engine, GameVariantRuntime runtime, SpriteAnimationSystem spriteAnimationSystem, GameLevel level) {
        final GameVariantUIConfig uiConfig = engine.uiConfig(runtime);
        final ActorSpriteAnimController animController = runtime.systems().actorSpriteAnimController();

        final Pac pac = level.entitySet().pac();
        if (animController.hasNoAnimations(pac)) {
            animController.setAnimations(pac, uiConfig.renderConfig().createPacAnimations(spriteAnimationSystem));
            resetPacAnimation(animController, pac);
        }

        level.entitySet().ghosts().forEach(ghost -> {
            if (animController.hasNoAnimations(ghost)) {
                animController.setAnimations(ghost,
                    uiConfig.renderConfig().createGhostAnimations(spriteAnimationSystem, ghost.personality()));
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
