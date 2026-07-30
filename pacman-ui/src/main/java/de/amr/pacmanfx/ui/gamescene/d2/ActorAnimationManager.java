/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.ActorAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

//TODO make anim systems for ghost and Pac-Man
public class ActorAnimationManager {

    public static void ensureActorAnimationsCreated(GameAppContext appContext, GameLevel level) {
        final GameVariantRenderConfig renderConfig = appContext.variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer animationContainer = appContext.ui().sprites().animations();

        final SpriteAnimSystem animSystem = appContext.currentGameContext().systems().spriteAnim();

        final Pac pac = level.entities().pac();
        if (animSystem.hasNoAnimations(pac)) {
            animSystem.setAnimations(pac, renderConfig.createPacAnimations(animationContainer));
            resetPacAnimation(animSystem, pac);
        }

        level.entities().ghosts().forEach(ghost -> {
            if (animSystem.hasNoAnimations(ghost)) {
                animSystem.setAnimations(ghost,
                    renderConfig.createGhostAnimations(animationContainer, ghost.personality()));
                resetGhostAnimation(animSystem, ghost);
            }
        });
    }

    // Called from game event handler
    public static void resetActorAnimations(SpriteAnimSystem animSystem, GameLevel level) {
        resetPacAnimation(animSystem, level.entities().pac());
        level.entities().ghosts().forEach(ghost -> resetGhostAnimation(animSystem, ghost));
    }

    public static void resetPacAnimation(SpriteAnimSystem animSystem, Pac pac) {
        animSystem.select(pac, ActorAnimationID.PAC_MUNCHING);
        animSystem.resetSelected(pac);
    }

    public static void resetGhostAnimation(SpriteAnimSystem animSystem, Ghost ghost) {
        animSystem.select(ghost, ActorAnimationID.GHOST_NORMAL);
        animSystem.resetSelected(ghost);
    }
}
