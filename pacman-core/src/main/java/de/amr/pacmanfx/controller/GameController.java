/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Score;
import javafx.beans.property.*;
import org.tinylog.Logger;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Controller (in the sense of MVC) for all game variants.
 * <br>Contains a finite-state machine ({@link StateMachine}) with states defined in {@link PacManGamesState}.
 * Each game variant is represented by an instance of a game model ({@link Game}).
 * Scene selection is not controlled by this class but left to the specific user interface implementations.
 *
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class GameController implements GameContext, GameEventManager, CoinMechanism {

    public static GameController THE_GAME_CONTROLLER;

    public static final Pattern GAME_VARIANT_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z_0-9]*");

    private final File homeDir = new File(System.getProperty("user.home"), ".pacmanfx");
    private final File customMapDir = new File(homeDir, "maps");

    private final StateMachine<FsmState<GameContext>, GameContext> stateMachine;
    private final Map<String, Game> knownGames = new HashMap<>();

    private boolean eventsEnabled;

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);
    private final BooleanProperty immunity = new SimpleBooleanProperty(false);
    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);
    private final StringProperty gameVariant = new SimpleStringProperty();

    public GameController(List<FsmState<GameContext>> states) {
        boolean success = initUserDirectories();
        if (!success) {
            throw new IllegalStateException("User directories could not be created");
        }

        stateMachine = new StateMachine<>(states, this);
        stateMachine.setName("Game Controller State Machine");
        stateMachine.addStateChangeListener((oldState, newState) -> publishEvent(new GameStateChangeEvent(game(), oldState, newState)));

        gameVariant.addListener((py, ov, newGameVariant) -> {
            if (eventsEnabled) {
                Game newGame = game(newGameVariant);
                newGame.init();
                publishEvent(GameEventType.GAME_VARIANT_CHANGED);
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

    public StateMachine<FsmState<GameContext>, GameContext> stateMachine() {
        return stateMachine;
    }

    public FsmState<GameContext> stateByName(String name) {
        return stateMachine.states().stream()
            .filter(state -> state.name().equals(name))
            .findFirst().orElseThrow();
    }

    public void setEventsEnabled(boolean enabled) {
        eventsEnabled = enabled;
    }

    public void changeGameState(FsmState<GameContext> state) {
        requireNonNull(state);
        stateMachine.changeState(state);
    }

    public void letCurrentGameStateExpire() {
        stateMachine.letCurrentStateExpire();
    }

    public void resumePreviousGameState() {
        stateMachine.resumePreviousState();
    }

    public void restart(FsmState<GameContext> state) {
        stateMachine.restart(state);
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
        return this;
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
        return this;
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
    public FsmState<GameContext> gameState() {
        return stateMachine.state();
    }


    // CoinMechanism implementation

    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    @Override
    public IntegerProperty numCoinsProperty() { return numCoins; }

    @Override
    public int numCoins() { return numCoins.get(); }

    @Override
    public boolean isEmpty() { return numCoins() == 0; }

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

    // GameEventManager implementation

    private final List<GameEventListener> eventListeners = new ArrayList<>();

    @Override
    public void addEventListener(GameEventListener listener) {
        requireNonNull(listener);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
            Logger.info("{}: Game event listener registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void removeEventListener(GameEventListener listener) {
        requireNonNull(listener);
        boolean removed = eventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", getClass().getSimpleName(), listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void publishEvent(GameEvent event) {
        requireNonNull(event);
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    @Override
    public void publishEvent(GameEventType type) {
        requireNonNull(type);
        publishEvent(new GameEvent(game(), type));
    }

    @Override
    public void publishEvent(GameEventType type, Vector2i tile) {
        requireNonNull(type);
        publishEvent(new GameEvent(game(), type, tile));
    }

    // other stuff

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