/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import javafx.beans.property.*;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Container for all game variants.
 * <p>
 * Each game variant is represented by an instance of a game model (see {@link Game}).
 * Scene selection is not controlled by this class but left to the specific user interface implementations.
 */
public class GameBox implements GameContext, CoinMechanism {

    public static final Pattern GAME_VARIANT_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z_0-9]*");

    /**
     * Root directory under which user specific files are stored.
     * <p>Default: <code>$HOME/.pacmanfx</code> (Unix) or <code>%USERPROFILE%\.pacmanfx</code> (MS Windows)</p>
     */
    public static final File HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");

    /**
     * Directory where custom maps are stored (default: <code>&lt;home_dir&gt;/maps</code>).
     */
    public static final File CUSTOM_MAP_DIR = new File(HOME_DIR, "maps");

    private static final String HIGHSCORE_FILE_PATTERN = "highscore-%s.xml";

    private static final boolean DIRECTORY_CHECK_OK = initUserDirectories();

    private final Map<String, Game> knownGames = new HashMap<>();

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);
    private final BooleanProperty immunity = new SimpleBooleanProperty(false);
    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);
    private final StringProperty gameVariantName = new SimpleStringProperty();

    private boolean eventsEnabled;

    public GameBox() {
        if (!DIRECTORY_CHECK_OK) {
            throw new IllegalStateException("User directories could not be created");
        }

        gameVariantName.addListener((py, ov, newGameVariant) -> {
            if (eventsEnabled) {
                Game newGame = game(newGameVariant);
                newGame.init();
                newGame.publishGameEvent(GameEvent.Type.GAME_VARIANT_CHANGED);
            }
        });

        cheatUsed.addListener((py, ov, cheated) -> {
            if (cheated) {
                Score highScore = currentGame().scoreManager().highScore();
                if (highScore.isEnabled()) {
                    highScore.setEnabled(false);
                }
            }
        });
    }

    public void setEventsEnabled(boolean enabled) {
        eventsEnabled = enabled;
    }

    /**
     * @param variant game variant (e.g. "PACMAN", "MS_PACMAN", "MS_PACMAN_TENGEN", "PACMAN_XXL", "MS_PACMAN_XXL"
     * @param gameModel the game model implementing the game variant
     */
    public void registerGame(String variant, Game gameModel) {
        requireNonNull(variant);
        requireNonNull(gameModel);
        if (!GAME_VARIANT_PATTERN.matcher(variant).matches()) {
            throw new IllegalArgumentException(
                    "Game variant name '%s' does not match required syntax '%s'"
                            .formatted(variant, GAME_VARIANT_PATTERN));
        }
        Game existing = knownGames.putIfAbsent(variant, gameModel);
        if (existing != null) {
            Logger.warn("Game model ({}) already registered for variant {}",
                    existing.getClass().getName(), variant);
        }
    }

    public StringProperty gameVariantNameProperty() {
        return gameVariantName;
    }

    public String gameVariantName() { return gameVariantName.get(); }

    public void setGameVariantName(String gameVariantName) {
        requireNonNull(gameVariantName);
        this.gameVariantName.set(gameVariantName);
    }

    public boolean isCurrentGameVariant(String gameVariantName) {
        return requireNonNull(gameVariantName).equals(gameVariantName());
    }

    @SuppressWarnings("unchecked")
    public <T extends Game> T game(String variantName) {
        requireNonNull(variantName);
        if (knownGames.containsKey(variantName)) {
            return (T) knownGames.get(variantName);
        }
        String errorMessage = "Game variant named '%s' has not been registered!".formatted(variantName);
        Logger.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    // GameContext implementation

    @Override
    public String currentGameVariantName() {
        return gameVariantName();
    }

    @Override
    public <G extends Game> G currentGame() {
        G game = game(gameVariantName.get());
        if (game != null) {
            return game;
        }
        throw new IllegalStateException("No game is currently selected");
    }

    @Override
    public FsmState<GameContext> currentGameState() {
        return currentGame().stateMachine().state();
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return currentGame().optGameLevel();
    }

    @Override
    public GameLevel gameLevel() {
        return currentGame().optGameLevel().orElse(null);
    }

    // CoinMechanism implementation

    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    @Override
    public IntegerProperty numCoinsProperty() { return numCoins; }

    @Override
    public int numCoins() { return numCoins.get(); }

    @Override
    public boolean noCoin() { return numCoins() == 0; }

    @Override
    public void setNumCoins(int n) {
        if (n >= 0 && n <= CoinMechanism.MAX_COINS) {
            numCoins.set(n);
        } else {
            Logger.error("Cannot set number of coins to {}", n);
        }
    }

    @Override
    public void insertCoin() {
        setNumCoins(numCoins() + 1);
    }

    @Override
    public void consumeCoin() {
        if (numCoins() > 0) {
            setNumCoins(numCoins() - 1);
        }
    }

    @Override
    public BooleanProperty cheatUsedProperty() {
        return cheatUsed;
    }

    @Override
    public BooleanProperty immunityProperty() {
        return immunity;
    }

    @Override
    public BooleanProperty usingAutopilotProperty() {
        return usingAutopilot;
    }


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