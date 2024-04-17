/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.SimulationStepEventLog;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Controller (in the sense of MVC) for both (Pac-Man, Ms. Pac-Man) game variants.
 * <p>
 * A finite-state machine with states defined in {@link GameState}. The game data are stored in the model of the
 * selected game, see {@link GameVariants}. Scene selection is not controlled by this class but left to the specific user
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
public class GameController extends Fsm<GameState, GameModel> {

    public static final byte MAX_CREDIT = 99;

    private static final GameController IT = new GameController(GameVariants.PACMAN);

    /**
     * @return the game controller singleton
     */
    public static GameController it() {
        return IT;
    }

    private GameClock clock;
    private GameModel game;
    private boolean pacImmune = false;
    private int credit = 0;
    private SimulationStepEventLog eventLog;

    private GameController(GameModel variant) {
        super(GameState.values());
        selectGame(variant);
        // map FSM state change events to game events
        addStateChangeListener((oldState, newState) -> game.publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
    }

    public void selectGame(GameModel variant) {
        checkNotNull(variant);
        game = variant;
    }

    @Override
    public GameModel context() {
        return game;
    }

    public GameModel game() {
        return game;
    }

    @Override
    public void update() {
        eventLog = new SimulationStepEventLog();
        super.update();
        var messageList = eventLog.createMessageList();
        if (!messageList.isEmpty()) {
            Logger.info("During last step:");
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

    public SimulationStepEventLog eventLog() {
        return eventLog;
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