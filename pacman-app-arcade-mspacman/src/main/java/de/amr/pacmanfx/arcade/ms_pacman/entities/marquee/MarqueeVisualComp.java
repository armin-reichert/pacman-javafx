/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities.marquee;


import de.amr.pacmanfx.core.model.GameEntityComponent;
import javafx.scene.paint.Color;

public class MarqueeVisualComp implements GameEntityComponent {

    private Color bulbOnColor = Color.WHITE;
    private Color bulbOffColor = Color.GREEN;

    public Color bulbOnColor() {
        return bulbOnColor;
    }

    public void setBulbOnColor(Color bulbOnColor) {
        this.bulbOnColor = bulbOnColor;
    }

    public Color bulbOffColor() {
        return bulbOffColor;
    }

    public void setBulbOffColor(Color bulbOffColor) {
        this.bulbOffColor = bulbOffColor;
    }

    @Override
    public void reset() {
    }
}
