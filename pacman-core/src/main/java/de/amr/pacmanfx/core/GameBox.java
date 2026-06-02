/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Container for the playable games. Each game variant is represented by an instance of its game model (see {@link GameModel}).
 */
public class GameBox implements GameContext {

    /**
     * Game variant names must match this pattern (e.g. "MS_PACMAN_2024").
     */
    public static final Pattern GAME_VARIANT_NAME_PATTERN = Pattern.compile("[A-Z][A-Z_0-9]*");

    /**
     * Directory under which the user specific files are stored.
     * <p>Default: <code>$HOME/.pacmanfx</code> (Unix) or <code>%USERPROFILE%\.pacmanfx</code> (MS Windows)</p>
     */
    public static final File DEFAULT_HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");

    /**
     * Directory where custom maps are stored (default: <code>&lt;home_dir&gt;/maps</code>).
     */
    public static final File DEFAULT_CUSTOM_MAP_DIR = new File(DEFAULT_HOME_DIR, "maps");

    private final StringProperty gameVariantName = new SimpleStringProperty();
    private final File homeDir = DEFAULT_HOME_DIR;
    private final File customMapDir = DEFAULT_CUSTOM_MAP_DIR;
    private final CoinMechanism coinMechanism;
    private final Map<String, GameModel> gamesByVariantName = new HashMap<>();

    public GameBox(CoinMechanism coinMechanism) {
        this.coinMechanism = requireNonNull(coinMechanism);

        final boolean ok = validateUserDirs();
        if (!ok) {
            throw new IllegalStateException("GameBox: User directory validation failed");
        }
    }

    public File customMapDir() {
        return customMapDir;
    }

    public File homeDir() {
        return homeDir;
    }

    public File highScoreFile(String gameVariantName) {
        requireNonNull(gameVariantName);
        final String fileName = "highscore-%s.xml".formatted(gameVariantName).toLowerCase();
        return new File(homeDir, fileName);
    }

    /**
     * @param variantName game variant name (e.g. "PACMAN", "MS_PACMAN", "MS_PACMAN_TENGEN", "PACMAN_XXL", "MS_PACMAN_XXL")
     * @param game the game model implementing the game variant
     */
    public void registerGame(String variantName, AbstractGameModel game) {
        requireNonNull(variantName);
        requireNonNull(game);

        if (!GAME_VARIANT_NAME_PATTERN.matcher(variantName).matches()) {
            throw new IllegalArgumentException("Game variant name '%s' does not match required syntax '%s'"
                .formatted(variantName, GAME_VARIANT_NAME_PATTERN));
        }

        final GameModel previousGame = gamesByVariantName.putIfAbsent(variantName, game);
        if (previousGame != null) {
            Logger.warn("Game ({}) is already registered for variant {}", previousGame.getClass().getName(), variantName);
        }

        final File highScoreFile = highScoreFile(variantName);
        game.setHighScoreFile(highScoreFile);

        Logger.info("Game model {} registered as {}, high score file: {}",
            game.getClass().getSimpleName(), variantName, highScoreFile);
    }

    // GameContext implementation

    @Override
    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    @Override
    public StringProperty gameVariantNameProperty() {
        return gameVariantName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameModel> T gameForVariant(String variantName) {
        requireNonNull(variantName);
        if (gamesByVariantName.containsKey(variantName)) {
            return (T) gamesByVariantName.get(variantName);
        }
        final String errorMessage = "Game variant named '%s' has not been registered!".formatted(variantName);
        Logger.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    @Override
    public <G extends AbstractGameModel> G gameModel() {
        final String variantName = gameVariantName();
        return variantName == null ? null : gameForVariant(variantName);
    }

    @Override
    public boolean hasGameForVariantName(String variantName) {
        requireNonNull(variantName);
        return gamesByVariantName.containsKey(variantName);
    }

    // other stuff

    private boolean validateUserDirs() {
        return dirExistsAndIsWritable(DEFAULT_HOME_DIR, "Home directory")
            && dirExistsAndIsWritable(DEFAULT_CUSTOM_MAP_DIR, "Custom map directory");
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