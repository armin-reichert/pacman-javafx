/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.action.core.GameActionContext;

public class ActorAnimationManager {

    public static void ensureActorAnimationsCreated(GameActionContext actionContext, GameLevel level) {
        final GameVariantConfig config = actionContext.variants().currentVariant().config();
        final SpriteAnimationContainer animationContainer = actionContext.ui().sprites().animations();
        final Pac pac = level.entities().pac();
        if (pac.animations().isEmpty()) {
            pac.setAnimations(config.createPacAnimations(animationContainer));
            resetPacAnimation(pac);
        }
        level.entities().ghosts().forEach(ghost -> {
            if (ghost.animations().isEmpty()) {
                ghost.setAnimations(config.createGhostAnimations(animationContainer, ghost.personality()));
                resetGhostAnimation(ghost);
            }
        });
    }

    // Called from game event handler
    public static void resetActorAnimations(GameLevel level) {
        resetPacAnimation(level.entities().pac());
        level.entities().ghosts().forEach(ActorAnimationManager::resetGhostAnimation);
    }

    public static void resetPacAnimation(Pac pac) {
        pac.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pac.animations().resetSelected();
    }

    public static void resetGhostAnimation(Ghost ghost) {
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        ghost.animations().resetSelected();
    }
}
