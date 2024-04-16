/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.checkLevelNumber;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class GameLevel {

    /**
     * Level number; starts at 1.
     */
    public final int levelNumber;
    public final boolean demoLevel;
    private final byte[] data;

    public GameLevel(int levelNumber, boolean demoLevel, byte[] data) {
        checkLevelNumber(levelNumber);
        checkNotNull(data);
        this.levelNumber = levelNumber;
        this.demoLevel = demoLevel;
        this.data = data;
        Logger.trace("Game level {} created.", this.levelNumber);
    }

    /**
     * Relative Pac-Man speed (percentage of base speed)
     */
    public final byte pacSpeedPercentage() {
        return data[0];
    }

    /**
     * Relative ghost speed when hunting or scattering
     */
    public final byte ghostSpeedPercentage() {
        return data[1];
    }

    /**
     * Relative ghost speed inside tunnel
     */
    public final byte ghostSpeedTunnelPercentage() {
        return data[2];
    }

    /**
     * Number of pellets left when Blinky becomes "Cruise Elroy" grade 1
     */
    public final byte elroy1DotsLeft() {
        return data[3];
    }

    /**
     * Relative speed of Blinky being "Cruise Elroy" grade 1
     */
    public final byte elroy1SpeedPercentage() {
        return data[4];
    }

    /**
     * Number of pellets left when Blinky becomes "Cruise Elroy" grade 2
     */
    public final byte elroy2DotsLeft() {
        return data[5];
    }

    /**
     * Relative speed of Blinky being "Cruise Elroy" grade 2
     */
    public final byte elroy2SpeedPercentage() {
        return data[6];
    }

    /**
     * Relative speed of Pac-Man in power mode
     */
    public final byte pacSpeedPoweredPercentage() {
        return data[7];
    }

    /**
     * Relative speed of frightened ghost
     */
    public final byte ghostSpeedFrightenedPercentage() {
        return data[8];
    }

    /**
     * Number of seconds Pac-Man gets power
     */
    public final byte pacPowerSeconds() {
        return data[9];
    }

    /**
     * Number of maze flashes at end of this level
     */
    public final byte numFlashes() {
        return data[10];
    }

    /**
     * Number (1,2,3) of intermission scene played after this level (0=no intermission)
     */
    public final byte intermissionNumber() {
        return data[11];
    }

    public boolean isDemoLevel() {
        return demoLevel;
    }
}
