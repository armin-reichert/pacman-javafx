/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.app;

import de.amr.pacmanfx.model.GameModel;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Container for the playable games. Each game variant is represented by an instance of its game model (see {@link GameModel}).
 */
public class GamesContainer {

    public static File highScoreFile(String gameVariantName) {
        requireNonNull(gameVariantName);
        final String fileName = "highscore-%s.xml".formatted(gameVariantName).toLowerCase();
        return new File(AppConstants.USER_HOME_DIR, fileName);
    }


    private final Map<String, GameSpecification> gamesByVariantName = new HashMap<>();

    public GamesContainer() {
        final boolean ok = validateUserDirs();
        if (!ok) {
            throw new IllegalStateException("GameBox: User directory validation failed");
        }
    }

    /**
     * @param variantName game variant name (e.g. "PACMAN", "MS_PACMAN", "MS_PACMAN_TENGEN", "PACMAN_XXL", "MS_PACMAN_XXL")
     * @param game the game specification implementing the game variant
     */
    public void registerGame(String variantName, GameSpecification game) {
        requireNonNull(variantName);
        requireNonNull(game);

        if (!AppConstants.GAME_VARIANT_NAME_PATTERN.matcher(variantName).matches()) {
            throw new IllegalArgumentException("Game variant name '%s' does not match required syntax '%s'"
                .formatted(variantName, AppConstants.GAME_VARIANT_NAME_PATTERN));
        }

        final GameSpecification previousGame = gamesByVariantName.putIfAbsent(variantName, game);
        if (previousGame != null) {
            Logger.warn("Game ({}) is already registered for variant {}", previousGame.getClass().getName(), variantName);
        }

        final File highScoreFile = highScoreFile(variantName);
        game.gameModel().createHighScore(highScoreFile);

        Logger.info("Game model {} registered as {}, high score file: {}",
            game.getClass().getSimpleName(), variantName, highScoreFile);
    }

    public GameSpecification gameForVariant(String variantName) {
        requireNonNull(variantName);
        if (gamesByVariantName.containsKey(variantName)) {
            return gamesByVariantName.get(variantName);
        }
        final String errorMessage = "Game variant named '%s' has not been registered!".formatted(variantName);
        Logger.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    public boolean hasGameForVariantName(String variantName) {
        requireNonNull(variantName);
        return gamesByVariantName.containsKey(variantName);
    }

    // other stuff

    private boolean validateUserDirs() {
        return dirExistsAndIsWritable(AppConstants.USER_HOME_DIR, "Home directory")
            && dirExistsAndIsWritable(AppConstants.CUSTOM_MAP_DIR, "Custom map directory");
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