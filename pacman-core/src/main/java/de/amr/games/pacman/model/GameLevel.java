/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Pulse;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;

/**
 * @author Armin Reichert
 */
public class GameLevel {

    /** Level number; starts at 1. */
    public final int levelNumber;
    public final boolean demoLevel;

    private final byte[] data;
    private final World world;
    private final Pulse blinking;
    private final Pac pac;
    private final Ghost[] ghosts;

    public GameLevel(int levelNumber, boolean demoLevel, byte[] data, World world) {
        checkLevelNumber(levelNumber);
        checkNotNull(data);
        checkNotNull(world);
        this.levelNumber = levelNumber;
        this.demoLevel   = demoLevel;
        this.world = world;
        this.data = data;
        blinking = new Pulse(10, false);
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
}