/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

//TODO make individual animation systems for ghosts and Pac-Man?
public class ActorAnimationManager {

    public static void ensureActorAnimationsCreated(GameAppContext app, GameLevel level) {
        final GameVariant variant = app.gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animationContainer = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController = variant.config().systems().actorSpriteAnimController();

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
