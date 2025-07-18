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

import java.io.File;
import java.util.Optional;

class GameContextImpl implements GameContext {
    private final File homeDir = new File(System.getProperty("user.home"), ".pacmanfx");
    private final File customMapDir = new File(homeDir, "maps");
    private final GameController gameController = new GameController(this);

    @Override
    public CoinMechanism theCoinMechanism() {
        return theGameController().coinMechanism();
    }

    @Override
    public File theCustomMapDir() {
        return customMapDir;
    }

    @Override
    public <T extends GameModel> T theGame() {
        return gameController.currentGame();
    }

    @Override
    public GameController theGameController() {
        return gameController;
    }

    @Override
    public GameEventManager theGameEventManager() {
        return gameController.gameEventManager();
    }

    @Override
    public File theHomeDir() {
        return homeDir;
    }

    @Override
    public File theHighScoreFile() {
        String gameVariant = theGameController().selectedGameVariant();
        return new File(homeDir, "highscore-%s.xml".formatted(gameVariant).toLowerCase());
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return theGame().level();
    }

    @Override
    public GameLevel theGameLevel() {
        return theGame().level().orElse(null);
    }

    @Override
    public GameState theGameState() {
        return gameController.gameState();
    }
}
