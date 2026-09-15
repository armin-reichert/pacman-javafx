/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Disposable;
import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.ui.input.Input;
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

    private final Set<Cartridge> cartridgeSet = new HashSet<>(6);

    private final Input input = new Input();
    private final GameClock clock;
    private final DirectoryWatchdog watchdog;

    public GameBox(GameClock clock) {
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

    public void insertCartridges(Cartridge... cartridges) {
        for (var c : cartridges) {
            if (c == null) {
                Logger.error("NULL cartridge detected! Are you kidding me?");
            } else {
                if (cartridgeSet.add(c)) {
                    Logger.info("Cartridge {} inserted into machine", c.id().name());
                } else {
                    Logger.info("Cartridge {} already inserted", c.id().name());
                }
            }
        }
    }

    public Cartridge cartridgeByName(String name) {
        requireNonNull(name);
        return findCartridgeByName(name)
            .orElseThrow(() -> new IllegalArgumentException(
                "No cartridge for game variant '%s' exists".formatted(name))
        );
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

    private Optional<Cartridge> findCartridgeByName(String name) {
        return cartridgeSet.stream().filter(c -> c.id().name().equals(name)).findFirst();
    }

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