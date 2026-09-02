/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.function.Function;

public record HUD_Style(
    SpriteSheet spriteSheet,
    RectShort livesCounterSymbolSprite,
    RectShort[] bonusSymbolSprites,
    String scoreText,
    String highScoreText,
    Color scoreTextColor,
    Color scoreTextColorDisabled,
    Font scoreTextFont,
    String creditTextFormat,
    Font messageFont,
    Function<MessageType, Color> messageColor
) {}
