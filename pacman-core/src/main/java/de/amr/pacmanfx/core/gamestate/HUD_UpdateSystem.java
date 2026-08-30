/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;

public class HUD_UpdateSystem {

    public void update(HUD hud, GameContext game) {
        final GameSession session = game.session();
        final LivesCounter livesCounter = session.hud().livesCounter();

        int displayedLivesCount = session.numLives() - 1;

        // When a new game starts or a level starts or continues, Pac-Man is invisible for some short time.
        // During that time, the level counter shows an additional entry and Pac-Man seems to hop from the lives
        // counter into the maze when the level starts.
        if (session.optLevel().isPresent()) {
            displayedLivesCount = adjustLiveCountOnStart(displayedLivesCount, game.state(), session.level());
        }
        displayedLivesCount = Math.clamp(displayedLivesCount, 0, livesCounter.data().maxLivesShown());
        livesCounter.data().setNumLives(displayedLivesCount);

    }

    private int adjustLiveCountOnStart(int count, AbstractGameState gameState, GameLevel level) {
        final boolean starting = gameState.id() == CommonGameStateID.GAME_STARTING
            || gameState.id() == CommonGameStateID.GAME_OR_LEVEL_STARTING;
        final Pac pac = level.entities().pac();
        return starting && !pac.isVisible() ? count + 1 : count;
    }

}
