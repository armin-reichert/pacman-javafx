/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.GameModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Globals.theGameEventManager;
import static java.util.Objects.requireNonNull;

/**
 * Controller (in the sense of MVC) for all game variants.
 * <br>Contains a finite-state machine ({@link StateMachine}) with states defined in {@link GameState}.
 * Each game variant is represented by an instance of a game model ({@link GameModel}).
 * Scene selection is not controlled by this class but left to the specific user interface implementations.
 *
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class GameController  {

    private final Map<String, GameModel> knownGames = new HashMap<>();
    private final ObjectProperty<String> gameVariantPy = new SimpleObjectProperty<>();
    private final StateMachine<GameState, GameModel> stateMachine;

    public GameController() {
        stateMachine = new StateMachine<>(GameState.values()) {
            @Override public GameModel context() { return currentGame(); }
        };
        stateMachine.addStateChangeListener((oldState, newState) ->
            theGameEventManager().publishEvent(new GameStateChangeEvent(currentGame(), oldState, newState)));

        gameVariantPy.addListener((py, ov, newGameVariant) -> {
            GameModel newGame = game(newGameVariant);
            newGame.init();
            theGameEventManager().publishEvent(newGame, GameEventType.GAME_VARIANT_CHANGED);
        });
    }

    public void changeGameState(GameState state) {
        requireNonNull(state);
        stateMachine.changeState(state);
    }

    public void letCurrentGameStateExpire() {
        stateMachine.letCurrentStateExpire();
    }

    public void updateGameState() {
        stateMachine.update();
    }

    public void resumePreviousGameState() {
        stateMachine.resumePreviousState();
    }

    public void restart(GameState state) {
        stateMachine.restart(state);
    }

    public GameState gameState() { return stateMachine.state(); }

    @SuppressWarnings("unchecked")
    public <T extends GameModel> T game(String variant) {
        requireNonNull(variant);
        if (knownGames.containsKey(variant)) {
            return (T) knownGames.get(variant);
        }
        throw new IllegalArgumentException("Game variant '%s' is not supported".formatted(variant));
    }

    public void registerGame(String variant, GameModel gameModel) {
        requireNonNull(variant);
        requireNonNull(gameModel);
        if (knownGames.containsKey(variant)) {
            Logger.warn("Game model ({}) is already registered for game variant {}", gameModel.getClass().getName(), variant);
        }
        knownGames.put(variant, gameModel);
    }

    /**
     * @return The game (model) registered for the currently selected game variant.
     */
    public <GAME extends GameModel> GAME currentGame() {
        return game(gameVariantPy.get());
    }

    public String selectedGameVariant() { return gameVariantPy.get(); }

    public void selectGameVariant(String gameVariant) {
        requireNonNull(gameVariant);
        gameVariantPy.set(gameVariant);
    }

    public boolean isSelected(String gameVariant) {
        return requireNonNull(gameVariant) == gameVariantPy.get();
    }
}