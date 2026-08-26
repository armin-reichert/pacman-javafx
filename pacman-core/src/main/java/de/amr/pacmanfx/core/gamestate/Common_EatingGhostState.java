/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.level.GameLevel;

/**
 * When a ghost has been eaten by Pac-Man, the game play freezes for a second, the ghost is displayed by the
 * points earned and only ghost returning to the house or entering and exiting the house are updated.
 */
public final class Common_EatingGhostState extends GameState {

    public static final int FREEZE_TICKS = 60;

    public Common_EatingGhostState() {
        super(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
    }

    @Override
    public void onEnter(GameContext game) {
        timer().restartTicks(FREEZE_TICKS);

        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final Pac pac = level.entities().pac();

        systems.pacState().setState(pac, PacState.SLEEPING);
        pac.hide();
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            game.variant().gameFlow().resumePreviousState(game);
        }
    }

    @Override
    public void onExit(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameLevel level = game.session().level();

        final Pac pac = level.entities().pac();
        systems.pacState().setState(pac, PacState.ACTIVE);
        pac.show();

        level.entities().ghostsInState(GhostState.EATEN).forEach(eatenGhost ->
            systems.ghostState().changeGhostState(eatenGhost, GhostState.RETURNING_HOME));
    }
}
