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
     * Root directory under which user specific files are stored.
     * <p>Default: <code>$HOME/.pacmanfx</code> (Unix) or <code>%USERPROFILE%\.pacmanfx</code> (MS Windows)</p>
     */
    File theHomeDir();

    /**
     * Directory where custom maps are stored (default: <code>&lt;home_dir&gt;/maps</code>).
     */
    File theCustomMapDir();

    /**
     * @return the high score file for the currently selected game
     */
    File theHighScoreFile();

    CoinMechanism           theCoinMechanism();

    <T extends GameModel> T theGame();

    GameController          theGameController();

    GameEventManager        theGameEventManager();

    Optional<GameLevel>     optGameLevel();

    GameLevel               theGameLevel();

    GameState               theGameState();
}
