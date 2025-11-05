/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Controller (in the sense of MVC) for all game variants.
 * <br>Contains a finite-state machine ({@link StateMachine}) with states defined in {@link GameState}.
 * Each game variant is represented by an instance of a game model ({@link Game}).
 * Scene selection is not controlled by this class but left to the specific user interface implementations.
 *
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class GameController implements GameContext {

    public static GameController THE_GAME_CONTROLLER;

    public static final Pattern GAME_VARIANT_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z_0-9]*");

    private final File homeDir = new File(System.getProperty("user.home"), ".pacmanfx");
    private final File customMapDir = new File(homeDir, "maps");

    private final StateMachine<GameState, GameContext> gameStateMachine;
    private final GameEventManager gameEventManager;
    private boolean eventsEnabled;
    private final CoinMechanism coinMechanism = new CoinMechanism();
    private final Map<String, Game> knownGames = new HashMap<>();

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);
    private final BooleanProperty immunity = new SimpleBooleanProperty(false);
    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);
    private final StringProperty gameVariant = new SimpleStringProperty();

    public GameController(List<GameState> states) {
        boolean success = initUserDirectories();
        if (!success) {
            throw new IllegalStateException("User directories could not be created");
        }
        gameEventManager = new GameEventManager(this);

        gameStateMachine = new StateMachine<>(states, this);
        gameStateMachine.addStateChangeListener((oldState, newState) ->
            gameEventManager.publishEvent(new GameStateChangeEvent(game(), oldState, newState)));

        gameVariant.addListener((py, ov, newGameVariant) -> {
            if (eventsEnabled) {
                Game newGame = game(newGameVariant);
                newGame.init();
                gameEventManager.publishEvent(GameEventType.GAME_VARIANT_CHANGED);
            }
        });

        cheatUsed.addListener((py, ov, cheated) -> {
            if (cheated) {
                Score highScore = game().scoreManager().highScore();
                if (highScore.isEnabled()) {
                    highScore.setEnabled(false);
                }
            }
        });

    }

    public GameState stateByName(String name) {
        return gameStateMachine.states().stream()
            .filter(state -> state.name().equals(name))
            .findFirst().orElseThrow();
    }

    public void setEventsEnabled(boolean enabled) {
        eventsEnabled = enabled;
    }

    public void changeGameState(GameState state) {
        requireNonNull(state);
        gameStateMachine.changeState(state);
    }

    public void letCurrentGameStateExpire() {
        gameStateMachine.letCurrentStateExpire();
    }

    public void updateGameState() {
        gameStateMachine.update();
    }

    public void resumePreviousGameState() {
        gameStateMachine.resumePreviousState();
    }

    public void restart(GameState state) {
        gameStateMachine.restart(state);
    }

    @SuppressWarnings("unchecked")
    public <T extends Game> T game(String variant) {
        requireNonNull(variant);
        if (knownGames.containsKey(variant)) {
            return (T) knownGames.get(variant);
        }
        throw new IllegalArgumentException("Game variant '%s' is not supported".formatted(variant));
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
        gameModel.init();
    }

    public StringProperty gameVariantProperty() {
        return gameVariant;
    }

    public String gameVariant() { return gameVariant.get(); }

    public void setGameVariant(String gameVariantName) {
        requireNonNull(gameVariantName);
        this.gameVariant.set(gameVariantName);
    }

    public boolean isCurrentGameVariant(String gameVariantName) {
        return requireNonNull(gameVariantName).equals(gameVariant());
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
    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    @Override
    public File homeDir() {
        return homeDir;
    }

    @Override
    public File customMapDir() {
        return customMapDir;
    }

    /**
     * @return The game (model) registered for the currently selected game variant.
     */
    public <G extends Game> G game() {
        return game(gameVariant.get());
    }

    @Override
    public GameController gameController() {
        return this;
    }

    @Override
    public GameEventManager eventManager() {
        return gameEventManager;
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return game().optGameLevel();
    }

    @Override
    public GameLevel gameLevel() {
        return game().optGameLevel().orElse(null);
    }

    @Override
    public GameState gameState() {
        return gameStateMachine.state();
    }


    private boolean initUserDirectories() {
        String homeDirDesc = "Pac-Man JavaFX home directory";
        String customMapDirDesc = "Pac-Man JavaFX custom map directory";
        boolean success = ensureDirExistsAndWritable(homeDir, homeDirDesc);
        if (success) {
            Logger.info("{} exists and is writable: {}", homeDirDesc, homeDir);
            success = ensureDirExistsAndWritable(customMapDir, customMapDirDesc);
            if (success) {
                Logger.info("{} exists and is writable: {}", customMapDirDesc, customMapDir);
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