/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameContext;

/**
 * Mixin for player methods common to 2D and 3D play scene.
 *
 * @author Armin Reichert
 */
public interface PlaySceneSound {

    //TODO avoid code duplication with PlayScene2D
    default void updateSound(GameContext context) {
        if (context.game().isDemoLevel()) {
            return;
        }
        ensureSirenPlaying(context);
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure
            context.soundHandler().stopMunchingSound();
        }
        if (context.game().pac().isAlive()
                && context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible)) {
            context.soundHandler().playGhostReturningHomeSound();
        } else {
            context.soundHandler().stopGhostReturningHomeSound();
        }
    }

    //TODO avoid code duplication with PlayScene2D
    default void ensureSirenPlaying(GameContext context) {
        if (!context.game().isDemoLevel() && context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            context.soundHandler().ensureSirenPlaying(context.game().huntingPhaseIndex() / 2);
        }
    }
}
