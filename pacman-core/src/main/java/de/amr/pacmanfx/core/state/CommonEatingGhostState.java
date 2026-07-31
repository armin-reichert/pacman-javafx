/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.comp.ghost.GhostState;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.ghost.GhostStateSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;

/**
 * When a ghost has been eaten by Pac-Man, the game play freezes for a second, the ghost is displayed by the
 * points earned and only ghost returning to the house or entering and exiting the house are updated.
 */
public class CommonEatingGhostState extends GameState {

    private static final int FREEZE_TICKS = 60;

    public CommonEatingGhostState() {
        super(GameStateID.GAME_LEVEL_EATING_GHOST);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        timer().restartTicks(FREEZE_TICKS);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();

        level.heartbeat().triggerPulse();

        // Ensure ghosts that are returning home or accessing the house are being updated
        gameContext.systems().ghostState().update(gameContext);

        if (timer().hasExpired()) {
            level.entities().pac().show();

            final GhostStateSystem ghostStateSystem = gameContext.systems().ghostState();
            final SpriteAnimSystem spriteAnimSystem = gameContext.systems().spriteAnim();

            level.ghostsInState(GhostState.EATEN).forEach(
                ghost -> ghostStateSystem.changeState(gameContext, ghost, GhostState.RETURNING_HOME));

            level.entities().ghosts().forEach(spriteAnimSystem::playSelected);

            gameContext.flow().resumePreviousState(gameContext);
        }
    }
}
