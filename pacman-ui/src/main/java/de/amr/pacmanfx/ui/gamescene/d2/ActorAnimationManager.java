/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

public class ActorAnimationManager {

    public static void ensureActorAnimationsCreated(GameAppContext appContext, GameLevel level) {
        final GameVariantRenderConfig renderConfig = appContext.variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer animationContainer = appContext.ui().sprites().animations();
        final Pac pac = level.entities().pac();
        if (pac.assertComponent(SpriteAnim.class).animations().isEmpty()) {
            pac.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(animationContainer));
            resetPacAnimation(pac);
        }
        level.entities().ghosts().forEach(ghost -> {
            if (ghost.assertComponent(SpriteAnim.class).animations().isEmpty()) {
                ghost.assertComponent(SpriteAnim.class).setAnimations(
                    renderConfig.createGhostAnimations(animationContainer, ghost.personality()));
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
        pac.assertComponent(SpriteAnim.class).animations().select(CommonAnimationID.PAC_MUNCHING);
        pac.assertComponent(SpriteAnim.class).animations().resetSelected();
    }

    public static void resetGhostAnimation(Ghost ghost) {
        ghost.assertComponent(SpriteAnim.class).animations().select(CommonAnimationID.GHOST_NORMAL);
        ghost.assertComponent(SpriteAnim.class).animations().resetSelected();
    }
}
