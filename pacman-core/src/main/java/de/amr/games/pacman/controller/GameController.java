/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.lib.RuleBasedPacSteering;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import org.tinylog.Logger;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;
import static de.amr.games.pacman.lib.Globals.checkGameVariant;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * Controller (in the sense of MVC) for both (Pac-Man, Ms. Pac-Man) game variants.
 * <p>
 * A finite-state machine with states defined in {@link GameState}. The game data are stored in the model of the
 * selected game, see {@link GameModel}. Scene selection is not controlled by this class but left to the specific user
 * interface implementations.
 * <p>
 * <li>Exact level data for Ms. Pac-Man still not available. Any hints appreciated!
 * <li>Multiple players (1up, 2up) not implemented.</li>
 * </ul>
 *
 * @author Armin Reichert
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href= "https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class GameController extends Fsm<GameState, GameModel> {

    private static GameController it;

    /**
     * Creates the game controller singleton and sets the current game model to the given game variant.
     *
     * @param variant game variant to select
     */
    public static void create(GameVariant variant) {
        if (it != null) {
            throw new IllegalStateException("Game controller already created");
        }
        checkGameVariant(variant);
        it = new GameController(variant);
        Logger.info("Game controller created, selected game variant: {}", it.game.variant());
    }

    /**
     * @return the game controller singleton
     */
    public static GameController it() {
        if (it == null) {
            throw new IllegalStateException("Game Controller cannot be accessed before it has been created");
        }
        return it;
    }

    private final Steering autopilot = new RuleBasedPacSteering();
    private Steering manualPacSteering = Steering.NONE;
    private int credit;
    private boolean pacAutoControlled;
    private boolean pacImmune;
    private GameModel game;

    /**
     * Used in intermission test mode.
     */
    public int intermissionTestNumber;

    private GameController(GameVariant variant) {
        super(GameState.values());
        newGame(variant);
        // map FSM state change events to game events
        addStateChangeListener((oldState, newState) -> publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
    }

    public void newGame(GameVariant variant) {
        checkGameVariant(variant);
        game = new GameModel(variant);
    }

    @Override
    public GameModel context() {
        return game;
    }

    public GameModel game() {
        return game;
    }

    /**
     * @return number of coins inserted.
     */
    public int credit() {
        return credit;
    }

    public boolean setCredit(int credit) {
        if (0 <= credit && credit <= GameModel.MAX_CREDIT) {
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

    public boolean isPacAutoControlled() {
        return pacAutoControlled;
    }

    public void setPacAutoControlled(boolean pacAutoControlled) {
        this.pacAutoControlled = pacAutoControlled;
    }

    public void togglePacAutoControlled() {
        pacAutoControlled = !pacAutoControlled;
    }

    public boolean isPacImmune() {
        return pacImmune;
    }

    public void setPacImmune(boolean pacImmune) {
        this.pacImmune = pacImmune;
    }

    public Steering pacSteering() {
        return pacAutoControlled ? autopilot : manualPacSteering;
    }

    public Steering manualPacSteering() {
        return manualPacSteering;
    }

    public void setManualPacSteering(Steering steering) {
        checkNotNull(steering);
        this.manualPacSteering = steering;
    }
}