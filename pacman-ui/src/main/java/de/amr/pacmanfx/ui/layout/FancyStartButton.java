/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameAssets;
import de.amr.pacmanfx.uilib.widgets.FancyButton;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class FancyStartButton extends FancyButton {

    public FancyStartButton(GameAssets assets, Runnable action) {
        super(
            assets.translated("play_button"),
            assets.arcadeFont(30),
            Color.rgb(0, 155, 252, 0.7),
            Color.WHITE);
        setAction(action);
        StackPane.setAlignment(this, Pos.BOTTOM_CENTER);
    }
}