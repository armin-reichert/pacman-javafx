/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import javafx.beans.property.BooleanProperty;

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

    CoinMechanism coinMechanism();
}
