/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import javafx.beans.property.StringProperty;

public interface GameContext {

    StringProperty gameVariantNameProperty();

    /**
     * @return name (id) of the current game variant
     */
    default String gameVariantName() { return gameVariantNameProperty().get(); }

    /**
     * @return the game model of the currently selected game variant
     * @param <T> expected game model type
     * @throws ClassCastException if expected game model type does not match registered type
     */
    <T extends Game> T currentGame();

    /**
     * @return the coin mechanism of the game box
     */
    CoinMechanism coinMechanism();
}
