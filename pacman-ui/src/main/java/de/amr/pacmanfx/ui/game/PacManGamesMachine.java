/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.Disposable;
import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.uilib.GameClockFX;
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
public class PacManGamesMachine implements Disposable {

    // The lazy thread-safe singleton holder pattern
    private static class SingletonHolder {
        static final PacManGamesMachine SINGLETON = new PacManGamesMachine();
    }

    public static PacManGamesMachine instance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * High score file for game variant XYZ is stored as "highscore-xyz.xml" inside user home directory

     * @param variantName name of the game variant e.g. MS_PACMAN
     * @return highscore file name for this game variant
     */
    public static File highScoreFile(String variantName) {
        requireNonNull(variantName);
        final String fileName = "highscore-%s.xml".formatted(variantName).toLowerCase();
        return new File(GameConstants.USER_HOME_DIR, fileName);
    }

    private final Set<Cartridge> cartridges = new HashSet<>();
    private final Input input = new Input();
    private final CoinMechanism coinMechanism = new CoinMechanism(99);
    private final GameClock clock = new GameClockFX();
    private final DirectoryWatchdog watchdog;

    private PacManGamesMachine() {
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

    /**
     * Plugs the given cartridges into this machine.
     */
    public void plugInCartridges(Cartridge... cartridges) {
        for (var c : cartridges) {
            if (c == null) {
                Logger.error("NULL cartridge detected! Are you kdding me?");
            } else {
                plugInCartridge(c);
            }
        }
    }

    /**
     * Plugs the given cartridge into this machine.
     *
     * @param cartridge the game specification implementing the game variant
     */
    public void plugInCartridge(Cartridge cartridge) {
        requireNonNull(cartridge);

        if (!GameConstants.GAME_VARIANT_NAME_PATTERN.matcher(cartridge.name()).matches()) {
            throw new IllegalArgumentException("Game variant name '%s' does not match required syntax '%s'"
                .formatted(cartridge.name(), GameConstants.GAME_VARIANT_NAME_PATTERN));
        }

        final boolean added = cartridges.add(cartridge);
        if (added) {
            Logger.info("Cartridge {} inserted into machine", cartridge.name());
        }
        else {
            Logger.info("Cartridge {} already inserted", cartridge.name());
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
        return cartridges.stream().filter(cartridge -> cartridge.name().equals(name)).findFirst();
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