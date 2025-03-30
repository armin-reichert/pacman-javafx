/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.assertNotNull;

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

    private final Map<GameVariant, GameModel> gameModelMap = new EnumMap<>(GameVariant.class);
    private GameModel currentGameModel;
    public int credit;

    public GameController() {
        super(GameState.values());
        // map state change events to game events
        addStateChangeListener((oldState, newState) -> currentGameModel.publishGameEvent(new GameStateChangeEvent(currentGameModel, oldState, newState)));
        Logger.info("Game controller created");
    }

    /**
     * @return The currently selected game (model).
     */
    @SuppressWarnings("unchecked")
    public <GAME extends GameModel> GAME game() {
        return (GAME) currentGameModel;
    }

    public void setGameModel(GameVariant variant, GameModel gameModel) {
        assertNotNull(variant);
        assertNotNull(gameModel);
        gameModelMap.put(variant, gameModel);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameModel> T game(GameVariant variant) {
        assertNotNull(variant);
        return (T) gameModelMap.get(variant);
    }

    public Stream<GameModel> games() { return gameModelMap.values().stream(); }

    public void selectGameVariant(GameVariant variant) {
        assertNotNull(variant);
        GameVariant oldVariant = currentGameModel != null ? selectedGameVariant() : null;
        currentGameModel = game(variant);
        currentGameModel.init();
        if (oldVariant != variant) {
            currentGameModel.publishGameEvent(GameEventType.GAME_VARIANT_CHANGED);
        }
    }

    public boolean isGameVariantSelected(GameVariant gameVariant) {
        return gameModelMap.get(gameVariant) == currentGameModel;
    }

    public GameVariant selectedGameVariant() {
        return gameModelMap.entrySet().stream()
            .filter(entry -> entry.getValue() == currentGameModel)
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    @Override
    public GameModel context() {
        return currentGameModel;
    }

    @Override
    public void update() {
        currentGameModel.clearEventLog();
        super.update();
    }
}