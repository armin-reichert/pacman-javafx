/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Disposable;
import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.ui.input.Input;
import org.tinylog.Logger;

import java.io.File;

import static java.util.Objects.requireNonNull;

/**
 * Machine for playing Pac-Man game variants. Can be configured by plugging-in game cartridges.
 * <p>
 * Buy your cartridge now (super hot Black Friday deal: $0,99 per game)!
 * </p>
 */
public class GameBox implements Disposable {

    private final CartridgeRepository cartridgeRepository;
    private final Input input = new Input();
    private final CoinMechanism coinMechanism;
    private final GameClock clock;
    private final DirectoryWatchdog watchdog;

    public GameBox(CartridgeRepository cartridgeRepository, CoinMechanism coinMechanism, GameClock clock) {
        this.cartridgeRepository = requireNonNull(cartridgeRepository);
        this.coinMechanism = requireNonNull(coinMechanism);
        this.clock = requireNonNull(clock);
        clock.setTargetFrameRate(GameConstants.SIMULATION_FPS);

        final boolean ok = validateUserDirs();
        if (!ok) {
            throw new IllegalStateException("GameBox: User directory validation failed");
        }

        watchdog = new DirectoryWatchdog(GameConstants.CUSTOM_MAP_DIR);
    }

    @Override
    public void dispose() {
        clock.stop();
        watchdog.dispose();
    }

    public CartridgeRepository cartridgeRepository() {
        return cartridgeRepository;
    }

    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    public GameClock clock() {
        return clock;
    }

    public Input input() {
        return input;
    }

    public DirectoryWatchdog watchdog() {
        return watchdog;
    }

    // other stuff

    private boolean validateUserDirs() {
        return dirExistsAndIsWritable(GameConstants.USER_HOME_DIR, "Game root directory")
            && dirExistsAndIsWritable(GameConstants.CUSTOM_MAP_DIR, "Custom maps directory");
    }

    private static boolean dirExistsAndIsWritable(File dir, String description) {
        if (!dir.exists() && !dir.mkdirs()) {
            Logger.error("{} could not be created", description);
            return false;
        }
        if (!dir.canWrite()) {
            Logger.error("{} is not writable: {}", description, dir);
            return false;
        }
        Logger.info("{} exists and is writable: {}", description, dir);
        return true;
    }
}