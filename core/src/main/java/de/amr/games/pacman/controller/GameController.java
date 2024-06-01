/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.model.*;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Controller (in the sense of MVC) for all game variants.
 * <p>
 * This is a finite-state machine ({@link FiniteStateMachine}) with states defined in {@link GameState}.
 * Each game variant ({@link GameVariant}) is represented by an instance of a game model ({@link GameModel}).
 * <p>Scene selection is not controlled by this class but left to the specific user interface implementations.
 * <ul>
 * <li>Exact level data for Ms. Pac-Man still not available. Any hints appreciated!
 * <li>Multiple players (1up, 2up) not implemented.</li>
 * </ul>
 *
 * @author Armin Reichert
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class GameController extends FiniteStateMachine<GameState, GameModel> {

    private static final GameController SINGLE_INSTANCE = new GameController();
    public static GameController it() {
        return SINGLE_INSTANCE;
    }

    /** Maximum number of coins, as in MAME. */
    public static final byte MAX_CREDIT = 99;

    private final Map<GameVariant, GameModel> models = new EnumMap<>(GameVariant.class);
    {
        models.put(GameVariant.MS_PACMAN,  new MsPacManGame());
        models.put(GameVariant.PACMAN,     new PacManGame());
        models.put(GameVariant.PACMAN_XXL, new PacManXXLGame());
    }

    private final List<GameVariant> supportedVariants = new ArrayList<>();
    private GameClock clock;
    private GameModel game;
    private boolean pacImmune = false;
    private int credit = 0;

    private static void ensureCustomMapDirExists() {
        var dir = GameModel.CUSTOM_MAP_DIR;
        if (dir.exists() && dir.isDirectory()) {
            return;
        }
        boolean created = dir.mkdirs();
        if (created) {
            Logger.info("User map dir created: {}", dir);
        } else {
            Logger.error("User map dir could not be created: {}", dir);
        }
    }

    private GameController() {
        super(GameState.values());
        ensureCustomMapDirExists();
        for (var model : models.values()) {
            model.init();
            Logger.info("Game (variant={}) initialized.", model.variant());
        }
        // map state change events to game events
        addStateChangeListener((oldState, newState) -> game.publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
    }

    public void setSupportedVariants(GameVariant...variants) {
        checkNotNull(variants);
        if (variants.length == 0) {
            Logger.error("No supported game variant specified");
            throw new IllegalArgumentException();
        }
        var noDuplicates = new LinkedHashSet<>(List.of(variants));
        if (noDuplicates.size() < variants.length) {
            Logger.warn("Detected duplicates in supported game variants!");
            Logger.warn("Variants specified: {}", List.of(variants));
        }
        supportedVariants.addAll(noDuplicates);
        game = models.get(supportedVariants.getFirst());
    }

    public List<GameVariant> supportedVariants() {
        return Collections.unmodifiableList(supportedVariants);
    }

    public GameModel game() {
        return game;
    }

    public GameModel game(GameVariant variant) {
        checkNotNull(variant);
        if (!models.containsKey(variant)) {
            Logger.error("No game model for variant {} exists", variant);
            throw new IllegalArgumentException();
        }
        return models.get(variant);
    }

    public void selectGameVariant(GameVariant variant) {
        checkNotNull(variant);
        if (!supportedVariants.contains(variant)) {
            Logger.error("Game variant {} is not supported", variant);
            return;
        }
        var oldVariant = game.variant();
        game = game(variant);
        if (oldVariant != variant) {
            game.publishGameEvent(GameEventType.GAME_VARIANT_CHANGED);
        }
    }

    @Override
    public GameModel context() {
        return game;
    }

    @Override
    public void update() {
        game.clearEventLog();
        super.update();
        var messageList = game.eventLog().createMessageList();
        if (!messageList.isEmpty()) {
            Logger.info("During last simulation step:");
            for (var msg : messageList) {
                Logger.info("- " + msg);
            }
        }
    }

    public void setClock(GameClock clock) {
        this.clock = checkNotNull(clock);
    }

    public GameClock clock() {
        return clock;
    }

    /**
     * @return number of coins inserted.
     */
    public int credit() {
        return credit;
    }

    public boolean setCredit(int credit) {
        if (0 <= credit && credit <= MAX_CREDIT) {
            this.credit = credit;
            return true;
        }
        return false;
    }

    public boolean changeCredit(int delta) {
        return setCredit(credit + delta);
    }

    public boolean hasCredit() {
        return credit > 0;
    }

    public boolean isPacImmune() {
        return pacImmune;
    }

    public void setPacImmune(boolean pacImmune) {
        this.pacImmune = pacImmune;
    }
}