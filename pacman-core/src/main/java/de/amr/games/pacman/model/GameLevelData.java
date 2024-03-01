package de.amr.games.pacman.model;

import java.util.Arrays;

/**
 * Each level is parameterized by the following data:
 *  <pre>
 * 	pacSpeedPercentage             = data[0];
 * 	ghostSpeedPercentage           = data[1];
 * 	ghostSpeedTunnelPercentage     = data[2];
 * 	elroy1DotsLeft                 = data[3];
 * 	elroy1SpeedPercentage          = data[4];
 * 	elroy2DotsLeft                 = data[5];
 * 	elroy2SpeedPercentage          = data[6];
 * 	pacSpeedPoweredPercentage      = data[7];
 * 	ghostSpeedFrightenedPercentage = data[8];
 * 	pacPowerSeconds                = data[9];
 * 	numFlashes                     = data[10];
 * 	intermissionNumber             = data[11];
 * 	</pre>
 */
public class GameLevelData {

    private final byte[] data;

    public GameLevelData(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Relative Pac-Man speed (percentage of base speed).
     */
    public final byte pacSpeedPercentage() {
        return data[0];
    }

    /**
     * Relative ghost speed when hunting or scattering.
     */
    public final byte ghostSpeedPercentage() {
        return data[1];
    }

    /**
     * Relative ghost speed inside tunnel.
     */
    public final byte ghostSpeedTunnelPercentage() {
        return data[2];
    }

    /**
     * Number of pellets left when Blinky becomes "Cruise Elroy" grade 1.
     */
    public final byte elroy1DotsLeft() {
        return data[3];
    }

    /**
     * Relative speed of Blinky being "Cruise Elroy" grade 1.
     */
    public final byte elroy1SpeedPercentage() {
        return data[4];
    }

    /**
     * Number of pellets left when Blinky becomes "Cruise Elroy" grade 2.
     */
    public final byte elroy2DotsLeft() {
        return data[5];
    }

    /**
     * Relative speed of Blinky being "Cruise Elroy" grade 2.
     */
    public final byte elroy2SpeedPercentage() {
        return data[6];
    }

    /**
     * Relative speed of Pac-Man in power mode.
     */
    public final byte pacSpeedPoweredPercentage() {
        return data[7];
    }

    /**
     * Relative speed of frightened ghost.
     */
    public final byte ghostSpeedFrightenedPercentage() {
        return data[8];
    }

    /**
     * Number of seconds Pac-Man gets power.
     */
    public final byte pacPowerSeconds() {
        return data[9];
    }

    /**
     * Number of maze flashes at end of this level.
     */
    public final byte numFlashes() {
        return data[10];
    }

    /**
     * Number of intermission scene played after this level (1-3. 0 = no intermission).
     */
    public final byte intermissionNumber() {
        return data[11];
    }


}
