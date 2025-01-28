/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;

/**
 * Controller (in the sense of MVC) for all game variants.
 * <p>
 * This is a finite-state machine ({@link FiniteStateMachine}) with states defined in {@link GameState}.
 * Each game variant ({@link GameVariant}) is represented by an instance of a game model ({@link GameModel}).
 * <p>Scene selection is not controlled by this class but left to the specific user interface implementations.
 * <ul>
 * <li>Exact level data for Ms. Pac-Man still not available. Any hints appreciated!
 * <li>Multiple players (1up, 2up) not implemented.</li>
 * </ul>
 *
 * @author Armin Reichert
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class GameController extends FiniteStateMachine<GameState, GameModel> {

    public static GameController it() { return THE_ONE; }

    public static final int MAX_COINS = 99;

    private static final GameController THE_ONE = new GameController();

    private final Map<GameVariant, GameModel> modelsByVariant = new EnumMap<>(GameVariant.class);
    private GameModel currentGame;
    public int credit;

    private GameController() {
        super(GameState.values());
        // map state change events to game events
        addStateChangeListener((oldState, newState) -> currentGame.publishGameEvent(new GameStateChangeEvent(currentGame, oldState, newState)));
        Logger.info("Game controller created");
    }

    /**
     * @return The currently selected game (model).
     */
    public GameModel currentGame() {
        return currentGame;
    }

    public void addGameImplementation(GameVariant variant, GameModel gameModel) {
        modelsByVariant.put(variant, gameModel);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameModel> T gameModel(GameVariant variant) {
        Globals.assertNotNull(variant);
        return (T) modelsByVariant.get(variant);
    }

    public void selectGame(GameVariant variant) {
        Globals.assertNotNull(variant);
        GameVariant oldVariant = currentGame != null ? currentGameVariant() : null;
        currentGame = gameModel(variant);
        if (oldVariant != variant) {
            currentGame.publishGameEvent(GameEventType.GAME_VARIANT_CHANGED);
        }
    }

    public GameVariant currentGameVariant() {
        return modelsByVariant.entrySet().stream()
            .filter(entry -> entry.getValue() == currentGame)
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    @Override
    public GameModel context() {
        return currentGame;
    }

    @Override
    public void update() {
        currentGame.clearEventLog();
        super.update();
    }
}