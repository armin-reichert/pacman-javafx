/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.marquee;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class MarqueeVisualComp implements GameEntityComponent {

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
