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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

    private final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>();

    public GameController() {
        super(GameState.values());
        gameVariantPy.addListener((py, ov, newGameVariant) -> {
            GameModel game = game(newGameVariant);
            game.init();
            game.publishGameEvent(GameEventType.GAME_VARIANT_CHANGED);

        });
        // map state change events to game events
        addStateChangeListener((oldState, newState) -> game().publishGameEvent(new GameStateChangeEvent(game(), oldState, newState)));
        Logger.info("Game controller created");
    }

    /**
     * @return The currently selected game (model).
     */
    public <GAME extends GameModel> GAME game() {
        return game(gameVariantPy.get());
    }

    public void defineGameModel(GameVariant variant, GameModel gameModel) {
        gameModelMap.put(assertNotNull(variant), assertNotNull(gameModel));
    }

    @SuppressWarnings("unchecked")
    public <T extends GameModel> T game(GameVariant variant) {
        assertNotNull(variant);
        return (T) gameModelMap.get(variant);
    }

    public Stream<GameModel> games() { return gameModelMap.values().stream(); }

    public ObjectProperty<GameVariant> gameVariantProperty() { return gameVariantPy; }

    public boolean isGameVariantSelected(GameVariant gameVariant) {
        return gameVariant == gameVariantPy.get();
    }

    @Override
    public GameModel context() {
        return game();
    }

    @Override
    public void update() {
        game().clearEventLog();
        super.update();
    }
}