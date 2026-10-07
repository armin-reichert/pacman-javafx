/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.marquee;

public class MarqueeColorsComp {

    private String bulbOnColor = "#fff";

    private String bulbOffColor = "333";

    public MarqueeColorsComp() {
    }

    public String bulbOnColor() {
        return bulbOnColor;
    }

    public void setBulbOnColor(String bulbOnColor) {
        this.bulbOnColor = bulbOnColor;
    }

    public String bulbOffColor() {
        return bulbOffColor;
    }

    public void setBulbOffColor(String bulbOffColor) {
        this.bulbOffColor = bulbOffColor;
    }
}
