/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import javafx.beans.property.BooleanProperty;

import static java.util.Objects.requireNonNull;

/**
 * Facade to give access to the main game components.
 */
public interface GameContext {

    /**
     * @return name (id) of the current game variant
     */
    String gameVariantName();

    /**
     * @param variantName name of game variant
     * @return {@code true} if the given name matched the name of the current game variant
     */
    default boolean isCurrentGameVariant(String variantName) {
        return gameVariantName().equals(requireNonNull(variantName));
    }
    /**
     * @return the model (in MVC sense) of the currently selected game variant.
     * @param <T> specific game model type
     */
    <T extends Game> T currentGame();

    /**
     * @return the current game state (the state of the game controller FSM).
     */
    FsmState<GameContext> currentGameState();

    CoinMechanism coinMechanism();

    BooleanProperty cheatUsedProperty();

    BooleanProperty immunityProperty();

    BooleanProperty usingAutopilotProperty();
}
