package de.amr.pacmanfx.tengenmspacman.entities.textview;

import static java.util.Objects.requireNonNull;

public class ShadesOfBlue {

    public enum BlueValue {
        VERY_DARK_BLUE (0x01),
        DARK_BLUE      (0x11),
        MEDIUM_BLUE    (0x21),
        LIGHT_BLUE     (0x31);

        BlueValue(int nesPaletteIndex) {
            this.paletteIndex = nesPaletteIndex;
        }

        private final int paletteIndex;

        public int paletteIndex() {
            return paletteIndex;
        }
    }

    public ShadesOfBlue(BlueValue blueValue) {
        this.blueValue = requireNonNull(blueValue);
    }

    private BlueValue blueValue;

    public BlueValue blueValue() {
        return blueValue;
    }

    public void setBlueValue(BlueValue blueValue) {
        this.blueValue = requireNonNull(blueValue);
    }
}
