/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
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
 * Contains the games. Each game variant is represented by an instance of a game model (see {@link Game}).
 */
public class GameBox implements GameContext, CoinMechanism {

    /**
     * Game variant names must match this pattern (e.g. "MS_PACMAN_2024").
     */
    public static final Pattern GAME_VARIANT_NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z_0-9]*");

    /**
     * Directory under which the user specific files are stored.
     * <p>Default: <code>$HOME/.pacmanfx</code> (Unix) or <code>%USERPROFILE%\.pacmanfx</code> (MS Windows)</p>
     */
    public static final File HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");

    /**
     * Directory where custom maps are stored (default: <code>&lt;home_dir&gt;/maps</code>).
     */
    public static final File CUSTOM_MAP_DIR = new File(HOME_DIR, "maps");

    private static final String HIGHSCORE_FILE_PATTERN = "highscore-%s.xml";

    private static final boolean DIRECTORY_CHECK_OK = initUserDirectories();

    private final Map<String, Game> gamesByVariantName = new HashMap<>();

    private final StringProperty gameVariantName = new SimpleStringProperty();

    public GameBox() {
        if (!DIRECTORY_CHECK_OK) {
            throw new IllegalStateException("User directory check failed");
        }
        //gameVariantName.addListener((py, ov, newGameVariant) -> gameByVariantName(newGameVariant).init());
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
        final Game previousGameWithThisName = gamesByVariantName.putIfAbsent(variantName, game);
        if (previousGameWithThisName != null) {
            Logger.warn("Game ({}) was already registered for variant {}",
                    previousGameWithThisName.getClass().getName(), variantName);
        }
    }

    @Override
    public StringProperty gameVariantNameProperty() {
        return gameVariantName;
    }

    @SuppressWarnings("unchecked")
    public <T extends Game> T gameByVariantName(String variantName) {
        requireNonNull(variantName);
        if (gamesByVariantName.containsKey(variantName)) {
            return (T) gamesByVariantName.get(variantName);
        }
        String errorMessage = "Game variant named '%s' has not been registered!".formatted(variantName);
        Logger.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    // GameContext implementation

    @Override
    public <G extends Game> G currentGame() {
        G game = gameByVariantName(gameVariantName.get());
        if (game != null) {
            return game;
        }
        throw new IllegalStateException("No game is currently selected");
    }

    // CoinMechanism implementation

    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    @Override
    public CoinMechanism coinMechanism() {
        return this;
    }

    @Override
    public IntegerProperty numCoinsProperty() { return numCoins; }

    // other stuff

    public File highScoreFile(String gameVariant) {
        return new File(HOME_DIR, HIGHSCORE_FILE_PATTERN.formatted(gameVariant).toLowerCase());
    }

    private static boolean initUserDirectories() {
        String homeDirDesc = "Home directory";
        String customMapDirDesc = "Custom map directory";
        boolean success = ensureDirExistsAndWritable(HOME_DIR, homeDirDesc);
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