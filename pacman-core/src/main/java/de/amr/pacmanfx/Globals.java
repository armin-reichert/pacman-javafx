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

import java.io.File;
import java.util.Optional;
import java.util.Random;

import static java.util.Objects.requireNonNull;

public class Globals {

    // Ghost personalities
    public static final byte RED_GHOST_SHADOW = 0, PINK_GHOST_SPEEDY = 1, CYAN_GHOST_BASHFUL = 2, ORANGE_GHOST_POKEY = 3;

    public static final byte NUM_TICKS_PER_SEC = 60;

    /** Tile size=8px, half tile size=4px. */
    public static final int TS = 8, HTS = 4;

    private static GameContextImpl context;

    private static class GameContextImpl implements GameContext {
        private final File homeDir = new File(System.getProperty("user.home"), ".pacmanfx");
        private final File customMapDir = new File(homeDir, "maps");
        private final CoinMechanism coinMechanism = new CoinMechanism();
        private final GameEventManager gameEventManager = new GameEventManager();
        private final GameController gameController = new GameController(gameEventManager);
        private final Random random = new Random();
        private final SimulationStep simulationStep = new SimulationStep();

        @Override
        public CoinMechanism theCoinMechanism() {
            return coinMechanism;
        }

        @Override
        public File theCustomMapDir() {
            return customMapDir;
        }

        @Override
        public GameModel theGame() {
            return gameController.currentGame();
        }

        @Override
        public GameController theGameController() {
            return gameController;
        }

        @Override
        public GameEventManager theGameEventManager() {
            return gameEventManager;
        }

        @Override
        public File theHomeDir() {
            return homeDir;
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

        @Override
        public Random theRNG() {
            return random;
        }

        @Override
        public SimulationStep theSimulationStep() {
            return simulationStep;
        }
    }

    public static void initGameContext() {
        context = new GameContextImpl();
    }

    public static GameContext theGameContext() {
        requireNonNull(context, "Game context not initialized!");
        return context;
    }
}