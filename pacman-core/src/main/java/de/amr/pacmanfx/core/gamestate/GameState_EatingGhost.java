/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;

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
        final GameLevel level = game.session().level();

        timer().restartTicks(FREEZE_TICKS);
        level.entities().ghostsInState(GhostState.EATEN).forEach(eatenGhost -> {
            final int animationFrame = level.indexInGhostKilledChain(eatenGhost);
            if (animationFrame != -1) {
                eatenGhost.animationSelection().select(CommonSpriteAnimationID.GHOST_POINTS, animationFrame);
            }
        });
    }

    @Override
    public void onUpdate(GameContext game) {
        final EntityUpdater updater = game.variant().systems().entityUpdater();
        final GameLevel level = game.session().level();

        updater.updateEntities(game, level);
        if (timer().hasExpired()) {
            game.variant().gameFlow().resumePreviousState(game);
        }
    }

    @Override
    public void onExit(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameLevelEntitySet entities = game.session().level().entities();

        entities.pac().show();
        entities.ghostsInState(GhostState.EATEN).forEach(eatenGhost ->
            systems.ghostState().changeGhostState(eatenGhost, GhostState.RETURNING_HOME));
    }
}
