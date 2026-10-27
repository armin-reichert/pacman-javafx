/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.ui.entities.props.textview.TextView;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.level.GameLevel;

import static java.util.Objects.requireNonNull;

public class HUD_UpdateSystem {

    public void updateHUD(GameContext game, HUD hud) {
        requireNonNull(game);
        requireNonNull(hud);

        final GameSession session = game.session();
        final int numLives = session.numLives();

        final LivesCounter livesCounter = hud.livesCounter();
        livesCounter.data().setNumLives(numLives);

        // Normally the lives counter shows a Pac symbol for each remaining live (without the Pac inside the maze)
        // When a new game or a level starts/continues, Pac-Man is invisible for some short time. During this time,
        // the level counter shows an additional entry and Pac-Man seems to "hop" from the lives counter into the maze
        // when the level starts.
        int numLivesShown = numLives - 1;
        if (session.optLevel().isPresent()) {
            final GameLevel level = session.optLevel().get();
            final boolean starting = game.state().id() == CommonGameStateID.GAME_STARTING
                || game.state().id() == CommonGameStateID.GAME_OR_LEVEL_STARTING;
            if (starting && !level.entitySet().pac().isVisible()) {
                ++numLivesShown;
            }
        }
        numLivesShown = Math.clamp(numLivesShown, 0, livesCounter.data().maxLivesShown());
        livesCounter.data().setNumLivesShown(numLivesShown);

        final TextView creditDisplay = hud.creditDisplay();
        creditDisplay.data().setText("CREDIT %2d".formatted(game.coinMechanism().numCoins()));
    }
}
