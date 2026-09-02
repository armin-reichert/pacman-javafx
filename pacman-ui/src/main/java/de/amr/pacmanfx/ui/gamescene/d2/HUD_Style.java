/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public record HUD_Style(
    String scoreText,
    String highScoreText,
    Color scoreTextColor,
    Color scoreTextColorDisabled,
    Font scoreTextFont
) {}
