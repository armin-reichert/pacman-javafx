package de.amr.games.pacman.model;

/**
 * Game level parameters.
 *
 * @author Armin Reichert
 */
public record GameLevelData(
    /*
     * Relative Pac-Man speed (percentage of base speed).
     */
    byte pacSpeedPercentage,

    /*
     * Relative ghost speed when hunting or scattering.
     */
    byte ghostSpeedPercentage,

    /*
     * Relative ghost speed inside tunnel.
     */
    byte ghostSpeedTunnelPercentage,

    /*
     * Number of pellets left when Blinky becomes "Cruise Elroy" grade 1.
     */
    byte elroy1DotsLeft,

    /*
     * Relative speed of Blinky being "Cruise Elroy" grade 1.
     */
    byte elroy1SpeedPercentage,

    /*
     * Number of pellets left when Blinky becomes "Cruise Elroy" grade 2.
     */
    byte elroy2DotsLeft,

    /*
     * Relative speed of Blinky being "Cruise Elroy" grade 2.
     */
    byte elroy2SpeedPercentage,

    /*
     * Relative speed of Pac-Man in power mode.
     */
    byte pacSpeedPoweredPercentage,

    /*
     * Relative speed of frightened ghost.
     */
    byte ghostSpeedFrightenedPercentage,

    /*
     * Number of seconds Pac-Man gets power.
     */
    byte pacPowerSeconds,

    /*
     * Number of maze flashes at end of this level.
     */
    byte numFlashes,

    /*
     * Number of intermission scene played after this level (1-3. 0 = no intermission).
     */
    byte intermissionNumber)
{
    public GameLevelData(byte[] data) {
        this(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8],
            data[9], data[10], data[11]);
    }
}