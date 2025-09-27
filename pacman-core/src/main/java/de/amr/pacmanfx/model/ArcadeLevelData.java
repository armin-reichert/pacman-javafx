/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

public record ArcadeLevelData(
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
    byte numFlashes)                     // Number of maze flashes at end of this level
{
    public ArcadeLevelData(byte[] data) {
        this(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10]);
    }
}