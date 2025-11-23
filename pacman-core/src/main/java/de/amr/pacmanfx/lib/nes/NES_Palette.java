/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.nes;

/**
 * Got this info from the <a href="https://www.mesen.ca/">Mesen NES emulator</a>.
 */
public interface NES_Palette {

    String[] COLORS = {
        // 00,     01,      02,        03,      04,       05,       06,       07
        "666666", "002a88", "1412a7", "3b00a4", "5c007e", "6e0040", "6c0600", "561d00",

        // 08,     09,      0A,        0B,      0C,       0D,       0E,       0F
        "333500", "0b4800", "005200", "004f08", "00404d", "000000", "000000", "000000",

        // 10,     11,      12,       13,       14,        15,      16,       17
        "adadad", "155fd9", "4240ff", "7527fe", "a01acc", "b71e7b", "b53120", "994e00",

        // 18,    19,       1A,        1B,       1C,      1D,       1E,       1F
        "6b6d00", "388700", "0c9300", "008f32", "007c8d", "000000", "000000", "000000",

        // 20,    21,       22,       23,       24,       25,       26,       27
        "fffeff", "64b0ff", "9290ff", "c676ff", "f36aff", "fe6ecc", "fe8170", "ea9e22",

        // 28,    29,       2A,       2B,       2C,       2D,       2E,       2F
        "bcbe00", "88d800", "5ce430", "45e082", "48cdde", "4f4f4f", "000000", "000000",

        // 30,    31,       32,       33,       34,       35,       36,        37
        "fffeff", "c0dfff", "d3d2ff", "e8c8ff", "fbc2ff", "fec4ea", "feccc5", "f7d8a5",

        // 38,    39,       3A,       3B,       3C,       3D,       3E,       3F
        "e4e594", "cfef96", "bdf4ab", "b3f3cc", "b5ebf2", "b8b8b8", "000000", "000000",
    };

    /**
     * Returns the RGB color value for the NES palette entry at the given index.
     *
     * @param index byte number from range {@code 00} to {@code 3F}
     * @return color in RGB hex string notation e.g. "bcbe00"
     */
    static String color(int index) {
        if (isValidIndex(index)) {
            return COLORS[index];
        }
        throw new IllegalArgumentException("Illegal NES palette index: " + index);
    }

    static boolean isValidIndex(int index) {
        return 0 <= index && index < COLORS.length;
    }
}
