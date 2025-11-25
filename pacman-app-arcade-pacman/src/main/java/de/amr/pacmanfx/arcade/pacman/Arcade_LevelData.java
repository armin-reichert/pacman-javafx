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
    public static Arcade_LevelData of(int... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("No level data values specified");
        }
        if (values.length != 11) {
            throw new IllegalArgumentException("Illegal number of level values: %d (should be 11)".formatted(values.length));
        }
        return new Arcade_LevelData(
            (byte) values[0], (byte) values[1], (byte) values[2], (byte) values[3], (byte) values[4], (byte) values[5],
            (byte) values[6], (byte) values[7], (byte) values[8], (byte) values[9], (byte) values[10]);
    }
}