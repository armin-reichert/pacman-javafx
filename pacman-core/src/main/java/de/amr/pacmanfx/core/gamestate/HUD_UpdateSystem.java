/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.level.GameLevel;

public class HUD_UpdateSystem {

    public void update(HUD hud, GameContext game) {
        final GameSession session = game.session();
        final LivesCounter livesCounter = hud.livesCounter();

        // Normally the lives counter shows a Pac symbol for each remaining live (without the Pac inside the maze)
        // When a new game or a level starts/continues, Pac-Man is invisible for some short time. During this time,
        // the level counter shows an additional entry and Pac-Man seems to "hop" from the lives counter into the maze
        // when the level starts.
        int livesShown = session.numLives() - 1;
        if (session.optLevel().isPresent()) {
            final GameLevel level = session.level();
            final boolean starting = game.state().id() == CommonGameStateID.GAME_STARTING
                || game.state().id() == CommonGameStateID.GAME_OR_LEVEL_STARTING;
            if (starting && !level.entities().pac().isVisible()) {
                ++livesShown;
            }
        }
        livesShown = Math.clamp(livesShown, 0, livesCounter.data().maxLivesShown());
        livesCounter.data().setNumLives(livesShown);
    }
}
