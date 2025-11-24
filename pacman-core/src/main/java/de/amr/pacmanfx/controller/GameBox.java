/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.GameEventType;
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
 * Container  for all game variants.
 * Each game variant is represented by an instance of a game model ({@link Game}).
 * Scene selection is not controlled by this class but left to the specific user interface implementations.
 *
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
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

    private final Map<String, Game> knownGames = new HashMap<>();

    private boolean eventsEnabled;

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);
    private final BooleanProperty immunity = new SimpleBooleanProperty(false);
    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);
    private final StringProperty gameVariant = new SimpleStringProperty();

    public GameBox() {
        boolean success = initUserDirectories();
        if (!success) {
            throw new IllegalStateException("User directories could not be created");
        }

        gameVariant.addListener((py, ov, newGameVariant) -> {
            if (eventsEnabled) {
                Game newGame = game(newGameVariant);
                newGame.init();
                newGame.stateMachine().publishEvent(GameEventType.GAME_VARIANT_CHANGED);
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

    @SuppressWarnings("unchecked")
    public <T extends Game> T game(String variant) {
        requireNonNull(variant);
        if (knownGames.containsKey(variant)) {
            return (T) knownGames.get(variant);
        }
        String errorMessage = "Game variant '%s' has not been registered!".formatted(variant);
        Logger.error(errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    /**
     * @param variant game variant (e.g. "PACMAN", "MS_PACMAN", "MS_PACMAN_TENGEN", "PACMAN_XXL", "MS_PACMAN_XXL"
     * @param gameModel the game model implementing the game variant
     */
    public void registerGame(String variant, Game gameModel) {
        requireNonNull(variant);
        if (!GAME_VARIANT_PATTERN.matcher(variant).matches()) {
            throw new IllegalArgumentException("Game variant name '%s' does not match required syntax '%s'"
                .formatted(variant, GAME_VARIANT_PATTERN));
        }
        requireNonNull(gameModel);
        if (knownGames.containsKey(variant)) {
            Logger.warn("Game model ({}) is already registered for game variant {}", gameModel.getClass().getName(), variant);
        }
        knownGames.put(variant, gameModel);
    }

    public StringProperty gameVariantNameProperty() {
        return gameVariant;
    }

    public String gameVariantName() { return gameVariant.get(); }

    public void setGameVariantName(String gameVariantName) {
        requireNonNull(gameVariantName);
        this.gameVariant.set(gameVariantName);
    }

    public boolean isCurrentGameVariant(String gameVariantName) {
        return requireNonNull(gameVariantName).equals(gameVariantName());
    }

    public BooleanProperty cheatUsedProperty() {
        return cheatUsed;
    }

    public BooleanProperty immunityProperty() {
        return immunity;
    }

    public BooleanProperty usingAutopilotProperty() {
        return usingAutopilot;
    }

    // GameContext implementation


    @Override
    public String currentGameVariantName() {
        return gameVariantName();
    }

    /**
     * @return The game (model) registered for the currently selected game variant.
     */
    public <G extends Game> G currentGame() {
        return game(gameVariant.get());
    }

    @Override
    public GameEventManager eventManager() {
        return currentGame().stateMachine();
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return currentGame().optGameLevel();
    }

    @Override
    public GameLevel gameLevel() {
        return currentGame().optGameLevel().orElse(null);
    }

    @Override
    public FsmState<GameContext> currentGameState() {
        return currentGame().stateMachine().state();
    }

    // CoinMechanism implementation

    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    @Override
    public IntegerProperty numCoinsProperty() { return numCoins; }

    @Override
    public int numCoins() { return numCoins.get(); }

    @Override
    public boolean containsNoCoin() { return numCoins() == 0; }

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

    // other stuff

    public File highScoreFile(String gameVariant) {
        return new File(HOME_DIR, "highscore-%s.xml".formatted(gameVariant).toLowerCase());
    }

    private boolean initUserDirectories() {
        String homeDirDesc = "Pac-Man JavaFX home directory";
        String customMapDirDesc = "Pac-Man JavaFX custom map directory";
        boolean success = ensureDirExistsAndWritable(HOME_DIR, homeDirDesc);
        if (success) {
            Logger.info("{} exists and is writable: {}", homeDirDesc, HOME_DIR);
            success = ensureDirExistsAndWritable(CUSTOM_MAP_DIR, customMapDirDesc);
            if (success) {
                Logger.info("{} exists and is writable: {}", customMapDirDesc, CUSTOM_MAP_DIR);
            }
            return true;
        }
        return false;
    }

    private boolean ensureDirExistsAndWritable(File dir, String description) {
        if (!dir.exists()) {
            Logger.info(description + " does not exist, create it...");
            if (!dir.mkdirs()) {
                Logger.error(description + " could not be created");
                return false;
            }
            Logger.info(description + " has been created");
            if (!dir.canWrite()) {
                Logger.error(description + " is not writable");
                return false;
            }
        }
        return true;
    }
}