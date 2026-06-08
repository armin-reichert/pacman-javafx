/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.model.GameModel;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Collection of the game variants. Each game variant is represented by an instance of its game model (see {@link GameModel}).
 */
public class GamesCollection {

    public static File highScoreFile(String gameVariantName) {
        requireNonNull(gameVariantName);
        final String fileName = "highscore-%s.xml".formatted(gameVariantName).toLowerCase();
        return new File(GameConstants.USER_HOME_DIR, fileName);
    }


    private final Map<String, GameVariantSpecification> gameSpecsByVariantName = new HashMap<>();

    public GamesCollection() {
        final boolean ok = validateUserDirs();
        if (!ok) {
            throw new IllegalStateException("GameBox: User directory validation failed");
        }
    }

    /**
     * @param variantName game variant name (e.g. "PACMAN", "MS_PACMAN", "MS_PACMAN_TENGEN", "PACMAN_XXL", "MS_PACMAN_XXL")
     * @param gameSpec the game specification implementing the game variant
     */
    public void registerGame(String variantName, GameVariantSpecification gameSpec) {
        requireNonNull(variantName);
        requireNonNull(gameSpec);

        if (!GameConstants.GAME_VARIANT_NAME_PATTERN.matcher(variantName).matches()) {
            throw new IllegalArgumentException("Game variant name '%s' does not match required syntax '%s'"
                .formatted(variantName, GameConstants.GAME_VARIANT_NAME_PATTERN));
        }

        final GameVariantSpecification previousGameSpec = gameSpecsByVariantName.putIfAbsent(variantName, gameSpec);
        if (previousGameSpec != null) {
            Logger.warn("Game spec ({}) is already registered for variant {}", previousGameSpec.getClass().getName(), variantName);
        }

        final File highScoreFile = highScoreFile(variantName);
        gameSpec.gameModel().createHighScore(highScoreFile);

        Logger.info("Game spec {} registered for variant {}, high-score file: {}",
            gameSpec.getClass().getSimpleName(), variantName, highScoreFile);
    }

    public GameVariantSpecification gameSpecForVariant(String variantName) {
        requireNonNull(variantName);
        if (gameSpecsByVariantName.containsKey(variantName)) {
            return gameSpecsByVariantName.get(variantName);
        }
        final String errorMessage = "No game spec was registered for game variant %s!".formatted(variantName);
        Logger.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    public boolean isGameVariantRegistered(String variantName) {
        requireNonNull(variantName);
        return gameSpecsByVariantName.containsKey(variantName);
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