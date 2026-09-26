/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.hud;

import de.amr.basics.ecs.GameEntityComp;
import de.amr.basics.math.RectShort;
import de.amr.basics.ui.assets.SpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public record HUD_Style(
    SpriteSheet<?> spriteSheet,
    RectShort livesCounterSymbolSprite,
    RectShort[] bonusSymbolSprites,
    String scoreText,
    String highScoreText,
    Color scoreTextColor,
    Color scoreTextColorDisabled,
    Font scoreTextFont,
    String creditTextFormat)
    implements GameEntityComp
{}
