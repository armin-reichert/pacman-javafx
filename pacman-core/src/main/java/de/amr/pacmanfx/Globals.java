/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameController;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Pac;

import java.io.File;
import java.util.Optional;
import java.util.Random;

import static java.util.Objects.requireNonNull;

/**
 * Global is not evil.
 *
 * @see <a href="https://www.youtube.com/watch?v=ogHl_OwcZWE">this video</a>
 */
public interface Globals {

    CoinMechanism    THE_COIN_MECHANISM     = new CoinMechanism();
    GameController   THE_GAME_CONTROLLER    = new GameController();
    GameEventManager THE_GAME_EVENT_MANAGER = new GameEventManager();
    Random           THE_RNG                = new Random();
    SimulationStep   THE_SIMULATION_STEP    = new SimulationStep();

    byte RED_GHOST_ID = 0, PINK_GHOST_ID = 1, CYAN_GHOST_ID = 2, ORANGE_GHOST_ID = 3;

    byte NUM_TICKS_PER_SEC = 60;

    /** Tile size=8px, half tile size=4px. */
    int TS = 8, HTS = 4;

    /**
     * Directory under which high scores, maps etc. are stored.
     * <p>Default: <code>&lt;user_home&gt;/.pacmanfx</code></p>
     */
    File HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");

    /**
     * Directory where custom maps are stored (default: <code>&lt;pacmanfx_home_dir&gt;/maps</code>).
     */
    File CUSTOM_MAP_DIR = new File(HOME_DIR, "maps");

    static GameModel game() { return THE_GAME_CONTROLLER.game(); }
    static GameVariant gameVariant() { return THE_GAME_CONTROLLER.selectedGameVariant(); }
    static GameState gameState() { return THE_GAME_CONTROLLER.state(); }
    static Optional<GameLevel> gameLevel() { return game().level(); }
    static Optional<Pac> pac() { return gameLevel().map(GameLevel::pac); }
}