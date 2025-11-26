/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import javafx.beans.property.BooleanProperty;

import java.util.Optional;

/**
 * Facade to give access to the main game components.
 */
public interface GameContext {

    /**
     * @return name (id) of the current game variant
     */
    String gameVariantName();

    /**
     * @return the model (in MVC sense) of the currently selected game variant.
     * @param <T> specific game model type
     */
    <T extends Game> T currentGame();

    /**
     * @return the current game state (the state of the game controller FSM).
     */
    FsmState<GameContext> currentGameState();

    /**
     * @return the current game level if present.
     */
    default Optional<GameLevel> optGameLevel() {
        return currentGame().optGameLevel();
    }

    /**
     * Convenience method to access the current game level.
     * <p>Returns {@code null} if no level is active. Use {@link #optGameLevel()} if you
     * need to handle the absence of a level safely.</p>
     *
     * @return the current {@link GameLevel}, or {@code null} if none exists
     */
    default GameLevel gameLevel() {
        return currentGame().optGameLevel().orElse(null);
    }

    BooleanProperty cheatUsedProperty();

    BooleanProperty immunityProperty();

    BooleanProperty usingAutopilotProperty();
}
