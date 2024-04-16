/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Pulse;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * @author Armin Reichert
 */
public class GameLevel {

    /** Level number; starts at 1. */
    public final int levelNumber;
    public final boolean demoLevel;

    private final byte[] data;
    private final TickTimer huntingTimer = new TickTimer("HuntingTimer");
    private final World world;
    private final Pulse blinking;
    private final Pac pac;
    private final Ghost[] ghosts;
    private final List<Byte> bonusSymbols;
    private Bonus bonus;
    public byte huntingPhaseIndex;
    public byte totalNumGhostsKilled;
    private byte cruiseElroyState;
    public byte bonusReachedIndex; // -1=no bonus, 0=first, 1=second

    public GameLevel(int levelNumber, boolean demoLevel, byte[] data, World world) {
        checkLevelNumber(levelNumber);
        checkNotNull(data);
        checkNotNull(world);
        this.levelNumber = levelNumber;
        this.demoLevel   = demoLevel;
        this.world = world;
        this.data = data;
        blinking = new Pulse(10, false);
        bonusSymbols = List.of(game().nextBonusSymbol(levelNumber), game().nextBonusSymbol(levelNumber));
        bonusReachedIndex = -1;
        pac = new Pac(game().pacName());
        ghosts = new Ghost[] {
            new Ghost(RED_GHOST,    game().ghostName(RED_GHOST)),
            new Ghost(PINK_GHOST,   game().ghostName(PINK_GHOST)),
            new Ghost(CYAN_GHOST,   game().ghostName(CYAN_GHOST)),
            new Ghost(ORANGE_GHOST, game().ghostName(ORANGE_GHOST))
        };
        Logger.trace("Game level {} created.", this.levelNumber);
    }

    /** Relative Pac-Man speed (percentage of base speed) */
    public final byte pacSpeedPercentage() { return data[0]; }

    /** Relative ghost speed when hunting or scattering */
    public final byte ghostSpeedPercentage() { return data[1]; }

    /** Relative ghost speed inside tunnel */
    public final byte ghostSpeedTunnelPercentage() { return data[2]; }

    /** Number of pellets left when Blinky becomes "Cruise Elroy" grade 1 */
    public final byte elroy1DotsLeft() { return data[3]; }

    /** Relative speed of Blinky being "Cruise Elroy" grade 1 */
    public final byte elroy1SpeedPercentage() { return data[4]; }

    /** Number of pellets left when Blinky becomes "Cruise Elroy" grade 2 */
    public final byte elroy2DotsLeft() { return data[5]; }

    /** Relative speed of Blinky being "Cruise Elroy" grade 2 */
    public final byte elroy2SpeedPercentage() { return data[6]; }

    /** Relative speed of Pac-Man in power mode */
    public final byte pacSpeedPoweredPercentage() { return data[7]; }

    /** Relative speed of frightened ghost */
    public final byte ghostSpeedFrightenedPercentage() { return data[8]; }

    /** Number of seconds Pac-Man gets power */
    public final byte pacPowerSeconds() { return data[9]; }

    /** Number of maze flashes at end of this level */
    public final byte numFlashes() { return data[10]; }

    /** Number (1,2,3) of intermission scene played after this level (0=no intermission) */
    public final byte intermissionNumber() { return data[11]; }

    public GameModel game() {
        return GameController.it().game();
    }

    public SimulationStepEventLog eventLog() {
        return GameController.it().eventLog();
    }

    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    public boolean isDemoLevel() {
        return demoLevel;
    }

    public World world() {
        return world;
    }

    public Pulse blinking() {
        return blinking;
    }

    public Pac pac() {
        return pac;
    }

    /**
     * @param id ghost ID, one of {@link GameModel#RED_GHOST}, {@link GameModel#PINK_GHOST},
     *           {@value GameModel#CYAN_GHOST}, {@link GameModel#ORANGE_GHOST}
     * @return the ghost with the given ID
     */
    public Ghost ghost(byte id) {
        checkGhostID(id);
        return ghosts[id];
    }

    /**
     * @param states states specifying which ghosts are returned
     * @return all ghosts which are in any of the given states or all ghosts, if no states are specified
     */
    public Stream<Ghost> ghosts(GhostState... states) {
        if (states.length > 0) {
            return Stream.of(ghosts).filter(ghost -> ghost.inState(states));
        }
        // when no states are given, return *all* ghosts (ghost.is() would return *no* ghosts!)
        return Stream.of(ghosts);
    }

    /**
     * @return Blinky's "cruise elroy" state. Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
     */
    public byte cruiseElroyState() {
        return cruiseElroyState;
    }

    /**
     * @param cruiseElroyState Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
     */
    public void setCruiseElroyState(int cruiseElroyState) {
        if (cruiseElroyState < -2 || cruiseElroyState > 2) {
            throw new IllegalArgumentException(
                "Cruise Elroy state must be one of -2, -1, 0, 1, 2, but is " + cruiseElroyState);
        }
        this.cruiseElroyState = (byte) cruiseElroyState;
        Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
    }

    public void enableCruiseElroyState(boolean enabled) {
        if (enabled && cruiseElroyState < 0 || !enabled && cruiseElroyState > 0) {
            cruiseElroyState = (byte) (-cruiseElroyState);
            Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
        }
    }

    public int totalNumGhostsKilled() {
        return totalNumGhostsKilled;
    }

    /**
     * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
     * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
     * ghosts attack Pac-Man.
     *
     * @param index hunting phase index (0..7)
     */
    public void startHuntingPhase(int index) {
        if (index < 0 || index > 7) {
            throw new IllegalArgumentException("Hunting phase index must be 0..7, but is " + index);
        }
        huntingPhaseIndex = (byte) index;
        var durations = game().huntingDurations(levelNumber);
        var ticks = durations[index] == -1 ? TickTimer.INDEFINITE : durations[index];
        huntingTimer.reset(ticks);
        huntingTimer.start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            index, currentHuntingPhaseName(), huntingTimer.duration(),
            (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
    }

    /**
     * @return number of current phase <code>(0-7)
     */
    public int huntingPhaseIndex() {
        return huntingPhaseIndex;
    }

    public Vector2i chasingTarget(byte ghostID) {
        return switch (ghostID) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> pac.tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> pac.tilesAheadWithOverflowBug(4);
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> pac.tilesAheadWithOverflowBug(2).scaled(2).minus(ghosts[RED_GHOST].tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghosts[ORANGE_GHOST].tile().euclideanDistance(pac.tile()) < 8
                ? ArcadeWorld.SCATTER_TILE_SW
                : pac.tile();
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public byte frightenedGhostRelSpeed(Ghost ghost) {
        return world.isTunnel(ghost.tile()) ? ghostSpeedTunnelPercentage() : ghostSpeedFrightenedPercentage();
    }

    /**
     * @return (optional) index of current scattering phase <code>(0-3)</code>
     */
    public Optional<Integer> scatterPhase() {
        return isEven(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    /**
     * @return (optional) index of current chasing phase <code>(0-3)</code>
     */
    public Optional<Integer> chasingPhase() {
        return isOdd(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    public String currentHuntingPhaseName() {
        return isEven(huntingPhaseIndex) ? "Scattering" : "Chasing";
    }

    /**
     * @param ghost a ghost
     * @return relative speed of ghost in percent of the base speed
     */
    public byte huntingSpeedPercentage(Ghost ghost) {
        if (world.isTunnel(ghost.tile())) {
            return ghostSpeedTunnelPercentage();
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 1) {
            return elroy1SpeedPercentage();
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 2) {
            return elroy2SpeedPercentage();
        }
        return ghostSpeedPercentage();
    }

    /* --- Here comes the main logic of the game. --- */


    // Bonus Management

    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    public byte bonusSymbol(int index) {
        return bonusSymbols.get(index);
    }

    /**
     * Called on bonus achievement (public access for unit tests and level test).
     *
     * @param index bonus index (0 or 1).
     */
    public void onBonusReached(int index) {
        if (index < 0 || index > 1) {
            throw new IllegalArgumentException("Bonus index must be 0 or 1");
        }
        bonus = game().createNextBonus(world, bonus, index, bonusSymbol(index)).orElse(null);
        if (bonus != null) {
            game().publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
        }
    }
}