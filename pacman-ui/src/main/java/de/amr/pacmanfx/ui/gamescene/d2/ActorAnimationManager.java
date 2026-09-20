/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.spriteanim.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.entities.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.action.core.GameApp;

//TODO make individual animation systems for ghosts and Pac-Man?
public class ActorAnimationManager {

    public static void ensureActorAnimationsCreated(GameApp app, GameLevel level) {
        final GameVariantRuntime variantRuntime = app.variantManager().currentRuntime();
        final GameVariantRenderConfig renderConfig = variantRuntime.uiConfig().renderConfig();
        final SpriteAnimationContainer animationContainer = variantRuntime.spriteAnimContainer();
        final ActorSpriteAnimController animController = variantRuntime.playConfig().systems().actorSpriteAnimController();

        final Pac pac = level.entities().pac();
        if (animController.hasNoAnimations(pac)) {
            animController.setAnimations(pac, renderConfig.createPacAnimations(animationContainer));
            resetPacAnimation(animController, pac);
        }

        level.entities().ghosts().forEach(ghost -> {
            if (animController.hasNoAnimations(ghost)) {
                animController.setAnimations(ghost,
                    renderConfig.createGhostAnimations(animationContainer, ghost.personality()));
                resetGhostAnimation(animController, ghost);
            }
        });
    }

    // Called from game event handler
    public static void resetActorAnimations(ActorSpriteAnimController animController, GameLevel level) {
        resetPacAnimation(animController, level.entities().pac());
        level.entities().ghosts().forEach(ghost -> resetGhostAnimation(animController, ghost));
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
