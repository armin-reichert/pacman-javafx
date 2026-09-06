package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.HUD_UpdateSystem;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.entities.GameOptionsDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.LevelNumberDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.gameoptionsdisplay.GameOptionsDataComp;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptions;

public class TengenMsPacMan_HUD_UpdateSystem extends HUD_UpdateSystem {

    public void update(HUD hud, GameContext game) {
        final GameSession session = game.session();

        final LivesCounter livesCounter = hud.livesCounter();
        livesCounter.data().setNumLives(session.numLives());

        // Normally the lives counter shows a Pac symbol for each remaining live (without the Pac inside the maze)
        // When a new game or a level starts/continues, Pac-Man is invisible for some short time. During this time,
        // the level counter shows an additional entry and Pac-Man seems to "hop" from the lives counter into the maze
        // when the level starts.
        int numLivesShown = session.numLives() - 1;
        if (session.optLevel().isPresent()) {
            final GameLevel level = session.level();
            final boolean starting = game.state().id() == CommonGameStateID.GAME_STARTING
                || game.state().id() == CommonGameStateID.GAME_OR_LEVEL_STARTING;
            if (starting && !level.entities().pac().isVisible()) {
                ++numLivesShown;
            }
        }
        numLivesShown = Math.clamp(numLivesShown, 0, livesCounter.data().maxLivesShown());
        livesCounter.data().setNumLivesShown(numLivesShown);

        final GameOptionsDisplay optionsDisplay = hud.entities().theOne(GameOptionsDisplay.class);
        if (gameOptions(session).areInitial()) {
            optionsDisplay.hide();
        } else {
            optionsDisplay.show();
        }

        final GameOptionsDataComp options = optionsDisplay.options();
        options.setBoosterMode(gameOptions(session).boosterMode());
        options.setDifficulty(gameOptions(session).difficulty());
        options.setMapCategory(gameOptions(session).mapCategory());

        final boolean showLevelNumber = gameOptions(session).mapCategory() != MapCategory.ARCADE;
        session.hud().entities().ofType(LevelNumberDisplay.class).forEach(display -> {
            if (showLevelNumber) {
                display.show();
            } else {
                display.hide();
            }
        });
    }
}
