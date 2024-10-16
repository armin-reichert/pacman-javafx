/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

/**
 * @author Armin Reichert
 */
public record GameLevel(
    byte pacSpeedPercentage,             // Relative Pac-Man speed (percentage of base speed)
    byte ghostSpeedPercentage,           // Relative ghost speed when hunting or scattering
    byte ghostSpeedTunnelPercentage,     // Relative ghost speed inside tunnel
    byte elroy1DotsLeft,                 // Number of pellets left when Blinky becomes "Cruise Elroy" grade 1
    byte elroy1SpeedPercentage,          // Relative speed of Blinky being "Cruise Elroy" grade 1
    byte elroy2DotsLeft,                 // Number of pellets left when Blinky becomes "Cruise Elroy" grade 2
    byte elroy2SpeedPercentage,          // Relative speed of Blinky being "Cruise Elroy" grade 2
    byte pacSpeedPoweredPercentage,      // Relative speed of Pac-Man in power mode
    byte ghostSpeedFrightenedPercentage, // Relative speed of frightened ghost
    byte pacPowerSeconds,                // Number of seconds Pac-Man gets power
    byte numFlashes,                     // Number of maze flashes at end of this level
    byte intermissionNumber)             // Number (1,2,3) of intermission scene played after this level (0=no intermission)
{
   public GameLevel(int... data) {
        this( (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3], (byte) data[4], (byte) data[5],
              (byte) data[6], (byte) data[7], (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11] );
    }
}