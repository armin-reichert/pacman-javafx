/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimSystem;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.level.GameLevel;

/**
 * When a ghost has been eaten by Pac-Man, the game play freezes for a second, the ghost is displayed by the
 * points earned and only ghost returning to the house or entering and exiting the house are updated.
 */
public final class GameState_EatingGhost extends GameState {

    private static final int FREEZE_TICKS = 60;

    public GameState_EatingGhost() {
        super(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        timer().restartTicks(FREEZE_TICKS);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GhostStateSystem ghostStateSystem = gameContext.systems().ghostState();
        final SpriteAnimSystem spriteAnimSystem = gameContext.systems().spriteAnim();
        final GameLevel level = gameContext.assertLevel();

        level.heartbeat().triggerPulse();

        level.entities().ghosts().stream()
            .filter(ghost -> GhostStateSystem.UPDATED_GHOST_STATES_WHILE_EATEN.contains(ghost.state()))
            .forEach(ghost -> ghostStateSystem.update(gameContext, ghost));

        if (timer().hasExpired()) {
            level.entities().pac().show();
            level.ghostsInState(GhostState.EATEN).forEach(
                ghost -> ghostStateSystem.changeState(gameContext, ghost, GhostState.RETURNING_HOME));
            level.entities().ghosts().forEach(spriteAnimSystem::playSelected);
            gameContext.flow().resumePreviousState(gameContext);
        }
    }
}
