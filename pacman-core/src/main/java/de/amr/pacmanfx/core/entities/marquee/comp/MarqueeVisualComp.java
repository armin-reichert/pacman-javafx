/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.marquee.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;

public class MarqueeVisualComp implements EntityComponent {

    private String bulbOnColor = "#fff";
    private String bulbOffColor = "333";

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

    @Override
    public void reset() {
    }
}
