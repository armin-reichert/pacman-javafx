/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.model.*;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Controller (in the sense of MVC) for both (Pac-Man, Ms. Pac-Man) game variants.
 * <p>
 * A finite-state machine with states defined in {@link GameState}. The game data are stored in the model of the
 * selected game, see {@link GameVariant}. Scene selection is not controlled by this class but left to the specific user
 * interface implementations.
 * <p>
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

    public static final byte MAX_CREDIT = 99;

    private final Map<GameVariant, GameModel> gameModels = new EnumMap<>(Map.of(
        GameVariant.MS_PACMAN,  new MsPacManGame(),
        GameVariant.PACMAN,     new PacManGame(),
        GameVariant.PACMAN_XXL, new PacManXXLGame()
    ));

    private final List<GameVariant> supportedGameVariants = new ArrayList<>();
    private GameClock clock;
    private GameModel game;
    private boolean pacImmune = false;
    private int credit = 0;

    private GameController() {
        super(GameState.values());
        game = gameModels.get(GameVariant.PACMAN);
        for (var model : gameModels.values()) {
            model.init();
            Logger.info("Game (variant={}) initialized.", model.variant());
        }
        createCustomMapDir();
        // map state change events to events of the selected game
        addStateChangeListener((oldState, newState) -> game.publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
    }

    public void setSupportedGameVariants(GameVariant...variants) {
        checkNotNull(variants);
        supportedGameVariants.addAll(List.of(variants));
    }

    public List<GameVariant> supportedGameVariants() {
        return Collections.unmodifiableList(supportedGameVariants);
    }

    private void createCustomMapDir() {
        var dir = GameModel.CUSTOM_MAP_DIR;
        if (dir.exists() && dir.isDirectory()) {
            return;
        }
        boolean created = dir.mkdirs();
        if (created) {
            Logger.info("User maps directory created: {}", dir);
        } else {
            Logger.error("User map dir {} could not be created", dir);
        }
    }

    public GameModel game() {
        return game;
    }

    public GameModel game(GameVariant variant) {
        checkNotNull(variant);
        return gameModels.get(variant);
    }

    public void selectGame(GameVariant variant) {
        game = game(variant);
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
        this.clock = clock;
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