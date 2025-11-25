/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

public record Arcade_LevelData(
        byte pctPacSpeed,             // Relative Pac-Man speed (percentage of base speed)
        byte pctGhostSpeed,           // Relative ghost speed when hunting or scattering
        byte pctGhostSpeedTunnel,     // Relative ghost speed inside tunnel
        byte numDotsLeftElroy1,       // Number of pellets left when Blinky becomes "Cruise Elroy" grade 1
        byte pctElroy1Speed,          // Relative speed of Blinky being "Cruise Elroy" grade 1
        byte numDotsLeftElroy2,       // Number of pellets left when Blinky becomes "Cruise Elroy" grade 2
        byte pctElroy2Speed,          // Relative speed of Blinky being "Cruise Elroy" grade 2
        byte pctPacSpeedPowered,      // Relative speed of Pac-Man in power mode
        byte pctGhostSpeedFrightened, // Relative speed of frightened ghost
        byte secPacPower,             // Number of seconds Pac-Man gets power
        byte numFlashes)              // Number of maze flashes at end of this level
{
    public Arcade_LevelData(
        int pctPacSpeed,
        int pctGhostSpeed,
        int pctGhostSpeedTunnel,
        int numDotsLeftElroy1,
        int pctElroy1Speed,
        int numDotsLeftElroy2,
        int pctElroy2Speed,
        int pctPacSpeedPowered,
        int pctGhostSpeedFrightened,
        int secPacPower,
        int numFlashes)
    {
        this(
            (byte) pctPacSpeed,
            (byte) pctGhostSpeed,
            (byte) pctGhostSpeedTunnel,
            (byte) numDotsLeftElroy1,
            (byte) pctElroy1Speed,
            (byte) numDotsLeftElroy2,
            (byte) pctElroy2Speed,
            (byte) pctPacSpeedPowered,
            (byte) pctGhostSpeedFrightened,
            (byte) secPacPower,
            (byte) numFlashes);
    }
}