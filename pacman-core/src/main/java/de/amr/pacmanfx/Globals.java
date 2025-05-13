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
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.SimulationStep;
import de.amr.pacmanfx.model.actors.Pac;

import java.io.File;
import java.util.Optional;
import java.util.Random;

/**
 * Global is not evil.
 *
 * @see <a href="https://www.youtube.com/watch?v=ogHl_OwcZWE">this video</a>
 */
public class Globals {

    // Ghost characters
    public static final byte RED_GHOST_SHADOW = 0, PINK_GHOST_SPEEDY = 1, CYAN_GHOST_BASHFUL = 2, ORANGE_GHOST_POKEY = 3;

    public static final byte NUM_TICKS_PER_SEC = 60;

    /** Tile size=8px, half tile size=4px. */
    public static final int TS = 8, HTS = 4;

    /**
     * Directory under which high scores, maps etc. are stored.
     * <p>Default: <code>&lt;user_home&gt;/.pacmanfx</code></p>
     */
    public static final File HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");

    /**
     * Directory where custom maps are stored (default: <code>&lt;pacmanfx_home_dir&gt;/maps</code>).
     */
    public static final File CUSTOM_MAP_DIR = new File(HOME_DIR, "maps");

    public static CoinMechanism theCoinMechanism() { return theCoinMechanism; }
    public static GameController theGameController() { return theGameController; }
    public static GameEventManager theGameEventManager() { return theGameEventManager; }
    public static Random theRNG() { return theRandomNumberGenerator; }
    public static SimulationStep theSimulationStep() { return theSimulationStep; }
    public static GameModel game() { return theGameController.game(); }
    public static GameVariant gameVariant() { return theGameController.selectedGameVariant(); }
    public static GameState gameState() { return theGameController.state(); }
    public static Optional<GameLevel> gameLevel() { return game().level(); }
    public static Optional<Pac> pac() { return gameLevel().map(GameLevel::pac); }


    private static final CoinMechanism theCoinMechanism = new CoinMechanism();
    private static final GameController theGameController = new GameController();
    private static final GameEventManager theGameEventManager = new GameEventManager();
    private static final Random theRandomNumberGenerator = new Random();
    private static final SimulationStep theSimulationStep = new SimulationStep();

}