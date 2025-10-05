/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

public record Arcade_LevelData(
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
    public Arcade_LevelData(
        int pacSpeedPct,
        int ghostSpeedPct,
        int ghostSpeedTunnelPct,
        int elroy1DotsLeft,
        int elroy1SpeedPct,
        int elroy2DotsLeft,
        int elroy2SpeedPct,
        int pacSpeedPoweredPct,
        int ghostSpeedFrightenedPct,
        int pacPowerSeconds,
        int numFlashes)
    {
        this(
            (byte) pacSpeedPct,
            (byte) ghostSpeedPct,
            (byte) ghostSpeedTunnelPct,
            (byte) elroy1DotsLeft,
            (byte) elroy1SpeedPct,
            (byte) elroy2DotsLeft,
            (byte) elroy2SpeedPct,
            (byte) pacSpeedPoweredPct,
            (byte) ghostSpeedFrightenedPct,
            (byte) pacPowerSeconds,
            (byte) numFlashes);
    }
}