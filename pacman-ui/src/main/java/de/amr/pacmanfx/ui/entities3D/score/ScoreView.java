/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.entities3D.score;

import javafx.scene.text.Text;

/**
 * Displays score and high score in 3D play scene.
 */
public class ScoreView {

    private final Text titleDisplay = new Text();

    private final Text textDisplay = new Text();

    public ScoreView() {
    }

    public Text titleDisplay() {
        return titleDisplay;
    }

    public Text textDisplay() {
        return textDisplay;
    }
}