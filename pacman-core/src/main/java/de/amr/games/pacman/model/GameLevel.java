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
public record GameLevel(int levelNumber, boolean demoLevel, byte[] data) {

    public GameLevel(int levelNumber, boolean demoLevel, byte[] data) {
        checkLevelNumber(levelNumber);
        checkNotNull(data);
        if (data.length != 12) {
            throw new IllegalArgumentException("Level data must contain 12 bytes");
        }
        this.levelNumber = levelNumber;
        this.demoLevel = demoLevel;
        this.data = data;
        Logger.trace("Game level {} created.", this.levelNumber);
    }

    /**
     * Relative Pac-Man speed (percentage of base speed)
     */
    public byte pacSpeedPercentage() {
        return data[0];
    }

    /**
     * Relative ghost speed when hunting or scattering
     */
    public byte ghostSpeedPercentage() {
        return data[1];
    }

    /**
     * Relative ghost speed inside tunnel
     */
    public byte ghostSpeedTunnelPercentage() {
        return data[2];
    }

    /**
     * Number of pellets left when Blinky becomes "Cruise Elroy" grade 1
     */
    public byte elroy1DotsLeft() {
        return data[3];
    }

    /**
     * Relative speed of Blinky being "Cruise Elroy" grade 1
     */
    public byte elroy1SpeedPercentage() {
        return data[4];
    }

    /**
     * Number of pellets left when Blinky becomes "Cruise Elroy" grade 2
     */
    public byte elroy2DotsLeft() {
        return data[5];
    }

    /**
     * Relative speed of Blinky being "Cruise Elroy" grade 2
     */
    public byte elroy2SpeedPercentage() {
        return data[6];
    }

    /**
     * Relative speed of Pac-Man in power mode
     */
    public byte pacSpeedPoweredPercentage() {
        return data[7];
    }

    /**
     * Relative speed of frightened ghost
     */
    public byte ghostSpeedFrightenedPercentage() {
        return data[8];
    }

    /**
     * Number of seconds Pac-Man gets power
     */
    public byte pacPowerSeconds() {
        return data[9];
    }

    /**
     * Number of maze flashes at end of this level
     */
    public byte numFlashes() {
        return data[10];
    }

    /**
     * Number (1,2,3) of intermission scene played after this level (0=no intermission)
     */
    public byte intermissionNumber() {
        return data[11];
    }
}
