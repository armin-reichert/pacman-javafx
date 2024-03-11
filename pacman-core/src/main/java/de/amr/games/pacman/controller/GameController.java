/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.RouteBasedSteering;
import de.amr.games.pacman.lib.RuleBasedPacSteering;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.ArcadeWorld;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;
import static de.amr.games.pacman.lib.Globals.checkGameVariant;
import static de.amr.games.pacman.lib.Globals.checkLevelNumber;
import static de.amr.games.pacman.model.world.ArcadeWorld.*;

/**
 * Controller (in the sense of MVC) for both (Pac-Man, Ms. Pac-Man) game variants.
 * <p>
 * A finite-state machine with states defined in {@link GameState}. The game data are stored in the model of the
 * selected game, see {@link GameModel}. Scene selection is not controlled by this class but left to the specific user
 * interface implementations.
 * <p>
 * <li>Exact level data for Ms. Pac-Man still not available. Any hints appreciated!
 * <li>Multiple players (1up, 2up) not implemented.</li>
 * </ul>
 *
 * @author Armin Reichert
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href= "https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class GameController extends Fsm<GameState, GameModel> {

    private static final GameController IT = new GameController(GameVariant.PACMAN);

    /**
     * @return the game controller singleton
     */
    public static GameController it() {
        return IT;
    }

    private GameModel game;
    private boolean playing = false;
    private boolean autopilotEnabled = false;
    private boolean pacImmune = false;
    private int credit = 0;

    private GameController(GameVariant variant) {
        super(GameState.values());
        newGame(variant);
        // map FSM state change events to game events
        addStateChangeListener((oldState, newState) -> publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
    }

    public void newGame(GameVariant variant) {
        checkGameVariant(variant);
        game = new GameModel(variant);
    }

    @Override
    public GameModel context() {
        return game;
    }

    public GameModel game() {
        return game;
    }

    /**
     * @return number of coins inserted.
     */
    public int credit() {
        return credit;
    }

    public boolean setCredit(int credit) {
        if (0 <= credit && credit <= GameModel.MAX_CREDIT) {
            this.credit = credit;
            return true;
        }
        return false;
    }

    public boolean changeCredit(int delta) {
        return setCredit(credit + delta);
    }

    public boolean hasCredit() {
        return credit > 0;
    }

    public boolean isAutopilotEnabled() {
        return autopilotEnabled;
    }

    public void setAutopilotEnabled(boolean autopilotEnabled) {
        this.autopilotEnabled = autopilotEnabled;
    }

    public void toggleAutopilotEnabled() {
        autopilotEnabled = !autopilotEnabled;
    }

    public boolean isPacImmune() {
        return pacImmune;
    }

    public void setPacImmune(boolean pacImmune) {
        this.pacImmune = pacImmune;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    /**
     * Starts new game level with the given number.
     *
     * @param levelNumber level number (starting at 1)
     */
    public void createAndStartLevel(int levelNumber) {
        checkLevelNumber(levelNumber);
        switch (game.variant()) {
            case MS_PACMAN -> {
                var level = new GameLevel(game, createMsPacManWorld(mapNumberMsPacMan(levelNumber)),
                    levelNumber, GameModel.levelData(levelNumber), false);
                level.setAutopilot(new RuleBasedPacSteering());
                game.setLevel(level);
                Logger.info("Level {} created ({})", levelNumber, game.variant());
                publishGameEvent(game, GameEventType.LEVEL_CREATED);
                if (levelNumber == 1) {
                    game.clearLevelCounter();
                }
                // In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
                // (also inside a level) whenever a bonus score is reached. At least that's what I was told.
                if (levelNumber <= 7) {
                    game.incrementLevelCounter(level.bonusSymbol(0));
                }
                // At this point, the animations of Pac-Man and the ghosts must have been created!
                level.letsGetReadyToRumble(false);
                Logger.info("Level {} started ({})", levelNumber, game.variant());
                publishGameEvent(game, GameEventType.LEVEL_STARTED);
            }
            case PACMAN -> {
                var level = new GameLevel(game, createPacManWorld(), levelNumber, GameModel.levelData(levelNumber), false);
                level.setAutopilot(new RuleBasedPacSteering());
                game.setLevel(level);
                Logger.info("Level {} created ({})", levelNumber, game.variant());
                publishGameEvent(game, GameEventType.LEVEL_CREATED);
                if (levelNumber == 1) {
                    game.clearLevelCounter();
                }
                game.incrementLevelCounter(level.bonusSymbol(0));
                // At this point, the animations of Pac-Man and the ghosts must have been created!
                level.letsGetReadyToRumble(false);
                Logger.info("Level {} started ({})", levelNumber, game.variant());
                publishGameEvent(game, GameEventType.LEVEL_STARTED);
            }
        }
    }

    /**
     * Creates and starts the demo game level ("attract mode"). Behavior of the ghosts is different from the original
     * Arcade game because they do not follow a predetermined path but change their direction randomly when frightened.
     * In Pac-Man variant, Pac-Man at least follows the same path as in the Arcade game, but in Ms. Pac-Man game, she
     * does not behave as in the Arcade game but hunts the ghosts using some goal-driven algorithm.
     */
    public void createAndStartDemoLevel() {
        switch (game.variant()) {
            case MS_PACMAN -> {
                GameLevel level = new GameLevel(game, createMsPacManWorld(1), 1, GameModel.levelData(1), true);
                level.setAutopilot(new RuleBasedPacSteering());
                game.setLevel(level);
                Logger.info("Demo level created ({})", game.variant());
                publishGameEvent(game, GameEventType.LEVEL_CREATED);
                // At this point, the animations of Pac-Man and the ghosts must have been created!
                level.letsGetReadyToRumble(true);
                Logger.info("Demo Level started ({})", game.variant());
                publishGameEvent(game, GameEventType.LEVEL_STARTED);
            }
            case PACMAN -> {
                GameLevel level = new GameLevel(game, createPacManWorld(), 1, GameModel.levelData(1), true);
                level.setAutopilot(new RouteBasedSteering(level.pac(), List.of(ArcadeWorld.PACMAN_DEMO_LEVEL_ROUTE)));
                game.setLevel(level);
                Logger.info("Demo level created ({})", game.variant());
                publishGameEvent(game, GameEventType.LEVEL_CREATED);
                // At this point, the animations of Pac-Man and the ghosts must have been created!
                level.letsGetReadyToRumble(true);
                Logger.info("Demo Level started ({})", game.variant());
                publishGameEvent(game, GameEventType.LEVEL_STARTED);
            }
        }
    }
}