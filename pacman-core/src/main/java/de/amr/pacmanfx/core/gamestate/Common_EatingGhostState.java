/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
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

    private GameSystems systems;
    private GameFlowController gameFlow;
    private GameLevel level;
    private Pac pac;

    public Common_EatingGhostState() {
        super(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
    }

    @Override
    public void onEnter(GameContext game) {
        timer().restartTicks(FREEZE_TICKS);

        systems = game.variant().systems();
        gameFlow = game.variant().gameFlow();
        level = game.session().level();
        pac = level.entities().pac();

        systems.pacState().setState(pac, PacState.SLEEPING);
        pac.hide();

        level.entities().ghostsInState(GhostState.EATEN).forEach(ghost ->
            ghost.animationSelection().setDisabled(true));
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            gameFlow.resumePreviousState(game);
        }
    }

    @Override
    public void onExit(GameContext game) {
        systems.pacState().setState(pac, PacState.ACTIVE);
        pac.show();
        level.entities().ghostsInState(GhostState.EATEN).forEach(ghost -> {
            ghost.state().setEnumValue(GhostState.RETURNING_HOME);
            ghost.animationSelection().setDisabled(false);
        });
    }
}
