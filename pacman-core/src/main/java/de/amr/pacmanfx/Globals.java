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

/**
 * Global is not evil.
 *
 * @see <a href="https://www.youtube.com/watch?v=ogHl_OwcZWE">this video</a>
 */
public class Globals {

    // Ghost personalities
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

    public static CoinMechanism           theCoinMechanism()    { return COIN_MECHANISM; }
    public static GameModel               theGame()             { return GAME_CONTROLLER.currentGame(); }
    public static GameController          theGameController()   { return GAME_CONTROLLER; }
    public static GameEventManager        theGameEventManager() { return GAME_EVENT_MANAGER; }
    public static Optional<GameLevel>     optGameLevel()        { return theGame().level(); }
    public static GameLevel               theGameLevel()        { return optGameLevel().orElse(null); }
    public static GameState               theGameState()        { return GAME_CONTROLLER.gameState(); }
    public static Random                  theRNG()              { return RANDOM; }
    public static SimulationStep          theSimulationStep()   { return SIMULATION_STEP; }

    private static final CoinMechanism    COIN_MECHANISM     = new CoinMechanism();
    private static final GameController   GAME_CONTROLLER    = new GameController();
    private static final GameEventManager GAME_EVENT_MANAGER = new GameEventManager();
    private static final Random           RANDOM             = new Random();
    private static final SimulationStep   SIMULATION_STEP    = new SimulationStep();
}