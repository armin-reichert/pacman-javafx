/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

public record ArcadeLevelData(
    byte pacSpeedPct,             // Relative Pac-Man speed (percentage of base speed)
    byte ghostSpeedPct,           // Relative ghost speed when hunting or scattering
    byte ghostSpeedTunnelPct,     // Relative ghost speed inside tunnel
    byte elroy1DotsLeft,          // Number of pellets left when Blinky becomes "Cruise Elroy" grade 1
    byte elroy1SpeedPct,          // Relative speed of Blinky being "Cruise Elroy" grade 1
    byte elroy2DotsLeft,          // Number of pellets left when Blinky becomes "Cruise Elroy" grade 2
    byte elroy2SpeedPct,          // Relative speed of Blinky being "Cruise Elroy" grade 2
    byte pacSpeedPoweredPct,      // Relative speed of Pac-Man in power mode
    byte ghostSpeedFrightenedPct, // Relative speed of frightened ghost
    byte pacPowerSeconds,         // Number of seconds Pac-Man gets power
    byte numFlashes)              // Number of maze flashes at end of this level
{
    public ArcadeLevelData(byte[] bytes) {
        this(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7], bytes[8], bytes[9], bytes[10]);
    }
}