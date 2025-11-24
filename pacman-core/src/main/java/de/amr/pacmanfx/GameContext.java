/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;

import java.util.Optional;

/**
 * Facade to give access to the main game components.
 */
public interface GameContext {

    String currentGameVariantName();

    /**
     * @return the model (in MVC sense) of the currently selected game variant.
     * @param <T> specific game model type
     */
    <T extends Game> T currentGame();

    /**
     * @return the event manager that is used to publish/subscribe to game events created by the model layer.
     */
    GameEventManager eventManager();

    /**
     * @return the current game level if present.
     */
    Optional<GameLevel> optGameLevel();

    /**
     * Convenience method to access the current game level.
     * <p>Returns {@code null} if no level is active. Use {@link #optGameLevel()} if you
     * need to handle the absence of a level safely.</p>
     *
     * @return the current {@link GameLevel}, or {@code null} if none exists
     */
    GameLevel gameLevel();

    /**
     * @return the current game state (the state of the game controller FSM).
     */
    FsmState<GameContext> currentGameState();
}
