/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameVariant;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.theGameEventManager;
import static java.util.Objects.requireNonNull;

/**
 * Controller (in the sense of MVC) for all game variants.
 * <p>
 * This is a finite-state machine ({@link StateMachine}) with states defined in {@link GameState}.
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
public class GameController extends StateMachine<GameState, GameModel> {

    private final Map<GameVariant, GameModel> registeredGameModels = new EnumMap<>(GameVariant.class);
    private final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>();

    public GameController() {
        super(GameState.values());
        addStateChangeListener((oldState, newState) -> theGameEventManager().publishEvent(
                new GameStateChangeEvent(game(), oldState, newState)));
        gameVariantPy.addListener((py, ov, newGameVariant) -> {
            GameModel game = game(newGameVariant);
            game.init();
            theGameEventManager().publishEvent(game, GameEventType.GAME_VARIANT_CHANGED);
        });
    }

    @Override
    public GameModel context() {
        return game();
    }

    @SuppressWarnings("unchecked")
    public <T extends GameModel> T game(GameVariant variant) {
        return (T) registeredGameModels.get(requireNonNull(variant));
    }

    public void register(GameVariant variant, GameModel gameModel) {
        registeredGameModels.put(requireNonNull(variant), requireNonNull(gameModel));
    }

    /**
     * @return The game (model) registered for the currently selected game variant.
     */
    public <GAME extends GameModel> GAME game() {
        return game(gameVariantPy.get());
    }

    public Stream<GameModel> games() { return registeredGameModels.values().stream(); }

    public GameVariant selectedGameVariant() { return gameVariantPy.get(); }

    public void select(GameVariant gameVariant) {
        requireNonNull(gameVariant);
        gameVariantPy.set(gameVariant);
    }

    public boolean isSelected(GameVariant gameVariant) {
        return requireNonNull(gameVariant) == gameVariantPy.get();
    }
}