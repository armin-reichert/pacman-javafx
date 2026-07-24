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
import de.amr.pacmanfx.uilib.GameClockImpl;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Machine for playing Pac-Man game variants. Can be configured by plugging-in game cartridges.
 * <p>
 * Buy your cartridge now (super hot Black Friday deal: $0,99 per game)!
 * </p>
 */
public class GameBox implements Disposable {

    // The lazy thread-safe singleton holder pattern
    private static class SingletonHolder {
        static final GameBox SINGLETON = new GameBox();
    }

    public static GameBox instance() {
        return SingletonHolder.SINGLETON;
    }

    private final Set<Cartridge> cartridges = new HashSet<>(6);
    private final Input input = new Input();
    private final CoinMechanism coinMechanism = new CoinMechanism(99);
    private final GameClock clock = new GameClockImpl();
    private final DirectoryWatchdog watchdog;

    private GameBox() {
        final boolean ok = validateUserDirs();
        if (!ok) {
            throw new IllegalStateException("GameBox: User directory validation failed");
        }
        clock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
        watchdog = new DirectoryWatchdog(de.amr.pacmanfx.core.GameConstants.CUSTOM_MAP_DIR);
    }

    @Override
    public void dispose() {
        clock.stop();
        watchdog.dispose();
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

    public void insertCartridges(Cartridge... cartridgesToInsert) {
        for (var cartridge : cartridgesToInsert) {
            if (cartridge == null) {
                Logger.error("NULL cartridge detected! Are you kidding me?");
            } else {
                if (!GameConstants.GAME_VARIANT_NAME_PATTERN.matcher(cartridge.id().name()).matches()) {
                    throw new IllegalArgumentException("Game variant name '%s' does not match required syntax '%s'"
                        .formatted(cartridge.id().name(), GameConstants.GAME_VARIANT_NAME_PATTERN));
                }
                final boolean added = cartridges.add(cartridge);
                if (added) {
                    Logger.info("Cartridge {} inserted into machine", cartridge.id().name());
                } else {
                    Logger.info("Cartridge {} already inserted", cartridge.id().name());
                }
            }
        }
    }

    public Cartridge cartridgeByName(String name) {
        requireNonNull(name);
        return findCartridgeByName(name).orElseThrow(
            () -> {
                final String errorMessage = "No cartridge for game variant %s has been inserted!".formatted(name);
                Logger.error(errorMessage);
                return new IllegalArgumentException(errorMessage);
            }
        );
    }

    public boolean containsCartridgeWithName(String name) {
        requireNonNull(name);
        return findCartridgeByName(name).isPresent();
    }

    // other stuff

    private Optional<Cartridge> findCartridgeByName(String name) {
        return cartridges.stream().filter(cartridge -> cartridge.id().name().equals(name)).findFirst();
    }

    private boolean validateUserDirs() {
        return dirExistsAndIsWritable(de.amr.pacmanfx.core.GameConstants.USER_HOME_DIR, "Game root directory")
            && dirExistsAndIsWritable(de.amr.pacmanfx.core.GameConstants.CUSTOM_MAP_DIR, "Custom maps directory");
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