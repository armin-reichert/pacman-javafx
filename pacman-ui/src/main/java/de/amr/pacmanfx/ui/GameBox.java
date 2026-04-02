/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameClock;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.uilib.GameClockFX;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Container for the playable games. Each game variant is represented by an instance of its game model (see {@link Game}).
 */
public class GameBox implements GameContext, CoinMechanism {

    /**
     * Game variant names must match this pattern (e.g. "MS_PACMAN_2024").
     */
    public static final Pattern GAME_VARIANT_NAME_PATTERN = Pattern.compile("[A-Z][A-Z_0-9]*");

    /**
     * Directory under which the user specific files are stored.
     * <p>Default: <code>$HOME/.pacmanfx</code> (Unix) or <code>%USERPROFILE%\.pacmanfx</code> (MS Windows)</p>
     */
    public static final File HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");

    /**
     * Directory where custom maps are stored (default: <code>&lt;home_dir&gt;/maps</code>).
     */
    public static final File CUSTOM_MAP_DIR = new File(HOME_DIR, "maps");

    private static final boolean DIRECTORY_CHECK_OK = initUserDirectories();

    private final Map<String, Game> gamesByVariantName = new HashMap<>();

    private final StringProperty gameVariantName = new SimpleStringProperty();

    private final GameClock clock = new GameClockFX();

    public GameBox() {
        if (!DIRECTORY_CHECK_OK) {
            throw new IllegalStateException("User directory check failed");
        }
    }

    public File highScoreFile(String gameVariantName) {
        requireNonNull(gameVariantName);
        final String fileName = "highscore-%s.xml".formatted(gameVariantName).toLowerCase();
        return new File(GameBox.HOME_DIR, fileName);
    }

    public File highScoreFile(GameVariant gameVariant) {
        return highScoreFile(gameVariant.name());
    }

    /**
     * @param variantName game variant name (e.g. "PACMAN", "MS_PACMAN", "MS_PACMAN_TENGEN", "PACMAN_XXL", "MS_PACMAN_XXL")
     * @param game the game model implementing the game variant
     */
    public void registerGame(String variantName, Game game) {
        requireNonNull(variantName);
        requireNonNull(game);
        if (!GAME_VARIANT_NAME_PATTERN.matcher(variantName).matches()) {
            throw new IllegalArgumentException(
                    "Game variant name '%s' does not match required syntax '%s'"
                            .formatted(variantName, GAME_VARIANT_NAME_PATTERN));
        }
        final Game previousGame = gamesByVariantName.putIfAbsent(variantName, game);
        if (previousGame != null) {
            Logger.warn("Game ({}) was already registered for variant {}", previousGame.getClass().getName(), variantName);
        }
    }

    // CoinMechanism implementation

    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    @Override
    public IntegerProperty numCoinsProperty() { return numCoins; }

    public int numCoins() {
        return numCoinsProperty().get();
    }

    @Override
    public int maxCoins() {
        return 99;
    }

    public boolean isEmpty() {
        return numCoins() == 0;
    }

    public void setNumCoins(int n) {
        if (n >= 0 && n <= maxCoins()) {
            numCoinsProperty().set(n);
        } else {
            Logger.error("Cannot set number of coins to {}", n);
        }
    }

    public void insertCoin() {
        if (numCoins() +1 <= maxCoins()) {
            setNumCoins(numCoins() + 1);
        }
    }

    public void consumeCoin() {
        if (numCoins() > 0) {
            setNumCoins(numCoins() - 1);
        }
    }

    // GameContext implementation

    @Override
    public GameClock clock() {
        return clock;
    }

    @Override
    public boolean isGameRegistered(String name) {
        requireNonNull(name);
        return gamesByVariantName.containsKey(name);
    }

    @Override
    public StringProperty gameVariantNameProperty() {
        return gameVariantName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Game> T gameByVariantName(String variantName) {
        requireNonNull(variantName);
        if (gamesByVariantName.containsKey(variantName)) {
            return (T) gamesByVariantName.get(variantName);
        }
        final String errorMessage = "Game variant named '%s' has not been registered!".formatted(variantName);
        Logger.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    @Override
    public <G extends Game> G game() {
        final String variantName = gameVariantName();
        return variantName == null ? null : gameByVariantName(variantName);
    }

    @Override
    public CoinMechanism coinMechanism() {
        return this;
    }

    // other stuff

    private static boolean initUserDirectories() {
        final String homeDirDesc = "Home directory";
        final String customMapDirDesc = "Custom map directory";
        final boolean success = ensureDirExistsAndWritable(HOME_DIR, homeDirDesc);
        if (success) {
            return ensureDirExistsAndWritable(CUSTOM_MAP_DIR, customMapDirDesc);
        }
        return false;
    }

    private static boolean ensureDirExistsAndWritable(File dir, String description) {
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