/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.level.GameLevel;

/**
 * When a ghost has been eaten by Pac-Man, the game play freezes for a second, the ghost is displayed by the
 * points earned and only ghost returning to the house or entering and exiting the house are updated.
 */
public final class GameState_EatingGhost extends GameState {

    public static final int FREEZE_TICKS = 60;

    public GameState_EatingGhost() {
        super(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameLevel level = game.session().assertLevel();

        timer().restartTicks(FREEZE_TICKS);

        level.entities().ghostsInState(GhostState.EATEN).forEach(ghost -> {
            final int frame = level.indexInGhostKilledChain(ghost);
            if (frame != -1) {
                ghost.animationSelection().select(CommonSpriteAnimationID.GHOST_POINTS, frame);
            }
        });
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameLevel level = game.session().assertLevel();

        //level.heartbeat().triggerPulse();
        systems.entityUpdater().updateEntities(game, level);

        if (timer().hasExpired()) {
            game.variant().gameFlow().resumePreviousState(game);
        }
    }

    @Override
    public void onExit(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameLevel level = game.session().assertLevel();
        final GhostStateSystem ghostStateSystem = systems.ghostState();

        level.entities().pac().show();
        level.entities().ghostsInState(GhostState.EATEN).forEach(
            ghost -> ghostStateSystem.changeGhostState(ghost, GhostState.RETURNING_HOME));
    }
}
