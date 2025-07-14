/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameController;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.SimulationStep;
import javafx.beans.property.BooleanProperty;

import java.io.File;
import java.util.Optional;

public interface GameContext {
    /**
     * Directory under which high scores, maps etc. are stored.
     * <p>Default: <code>&lt;user_home&gt;/.pacmanfx</code></p>
     */
    File theHomeDir();

    /**
     * Directory where custom maps are stored (default: <code>&lt;home_dir&gt;/maps</code>).
     */
    File theCustomMapDir();

    CoinMechanism           theCoinMechanism();
    <T extends GameModel> T theGame();
    GameController          theGameController();
    GameEventManager        theGameEventManager();
    Optional<GameLevel>     optGameLevel();
    GameLevel               theGameLevel();
    GameState               theGameState();

    /**
     * @return what happened in the current simulation step ("frame state")
     */
    SimulationStep          theSimulationStep();

    // Global properties
    BooleanProperty         propertyImmunity();
    BooleanProperty         propertyUsingAutopilot();
}
