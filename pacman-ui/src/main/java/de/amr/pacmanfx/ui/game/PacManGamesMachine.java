/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Collection of the playable game variants.
 * <p>
 * All games in a single box (only 1,99 € / game)!
 * </p>
 */
public class PacManGamesMachine {

    private final Map<String, Cartridge> cartridges = new HashMap<>();

    public PacManGamesMachine() {
        final boolean ok = validateUserDirs();
        if (!ok) {
            throw new IllegalStateException("GameBox: User directory validation failed");
        }
    }

    /**
     * @param variantName game variant name (e.g. "PACMAN", "MS_PACMAN", "MS_PACMAN_TENGEN", "PACMAN_XXL", "MS_PACMAN_XXL")
     * @param cartridge the game specification implementing the game variant
     */
    public void insertCartridge(String variantName, Cartridge cartridge) {
        requireNonNull(variantName);
        requireNonNull(cartridge);

        if (!GameConstants.GAME_VARIANT_NAME_PATTERN.matcher(variantName).matches()) {
            throw new IllegalArgumentException("Game variant name '%s' does not match required syntax '%s'"
                .formatted(variantName, GameConstants.GAME_VARIANT_NAME_PATTERN));
        }

        final Cartridge prevCartridge = cartridges.putIfAbsent(variantName, cartridge);
        if (prevCartridge != null) {
            Logger.warn("Game spec ({}) is already registered for variant {}", prevCartridge.getClass().getName(), variantName);
        }


        Logger.info("Game spec {} registered for variant {}", cartridge.getClass().getSimpleName(), variantName);
    }

    public Cartridge cartridgeForVariant(String variantName) {
        requireNonNull(variantName);
        if (cartridges.containsKey(variantName)) {
            return cartridges.get(variantName);
        }
        final String errorMessage = "No game spec was registered for game variant %s!".formatted(variantName);
        Logger.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    public boolean isCartridgeForVariantRegistered(String variantName) {
        requireNonNull(variantName);
        return cartridges.containsKey(variantName);
    }

    // other stuff

    private boolean validateUserDirs() {
        return dirExistsAndIsWritable(GameConstants.USER_HOME_DIR, "Home directory")
            && dirExistsAndIsWritable(GameConstants.CUSTOM_MAP_DIR, "Custom map directory");
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