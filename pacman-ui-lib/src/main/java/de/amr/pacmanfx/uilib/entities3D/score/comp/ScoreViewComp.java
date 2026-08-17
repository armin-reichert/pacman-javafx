/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.entities3D.score.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;
import javafx.scene.text.Text;

/**
 * Displays score and high score in 3D play scene.
 */
public class ScoreViewComp implements EntityComponent {

    private final Text titleDisplay = new Text();

    private final Text textDisplay = new Text();

    public ScoreViewComp() {
    }

    public Text titleDisplay() {
        return titleDisplay;
    }

    public Text textDisplay() {
        return textDisplay;
    }

    @Override
    public void reset() {
    }
}