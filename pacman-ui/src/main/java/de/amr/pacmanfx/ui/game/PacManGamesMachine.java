/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import org.tinylog.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Collection of the playable game variants.
 * <p>
 * All games in a single box (only 1,99 € / game)!
 * </p>
 */
public class PacManGamesMachine {

    private final Set<Cartridge> cartridges = new HashSet<>();

    public PacManGamesMachine() {
        final boolean ok = validateUserDirs();
        if (!ok) {
            throw new IllegalStateException("GameBox: User directory validation failed");
        }
    }

    /**
     * @param cartridge the game specification implementing the game variant
     */
    public void loadCartridge(Cartridge cartridge) {
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

    private Optional<Cartridge> findCartridgeByName(String name) {
        return cartridges.stream().filter(cartridge -> cartridge.name().equals(name)).findFirst();
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