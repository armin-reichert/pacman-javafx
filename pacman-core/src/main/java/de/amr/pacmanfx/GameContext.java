/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameController;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;

import java.io.File;
import java.util.Optional;

/**
 * Facade to give access to the main game components.
 */
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
     * @return the coin mechanism used in the Arcade games.
     */
    CoinMechanism theCoinMechanism();

    /**
     * @return the model (in MVC sense) of the currently selected game variant.
     * @param <T> specific game model type
     */
    <T extends Game> T theGame();

    /**
     * @return the controller (in MVC sense) used by all game variants. Implemented as a FSM.
     */
    GameController theGameController();

    /**
     * @return the event manager that is used to publish/subscribe to game events created by the model layer.
     */
    GameEventManager theGameEventManager();

    /**
     * @return the current game level if present.
     */
    Optional<GameLevel> optGameLevel();

    /**
     * @return the current game level if present or {@code null} if no game level currently exists.
     */
    GameLevel theGameLevel();

    /**
     * @return the current game state (the state of the game controller FSM).
     */
    GameState theGameState();
}
