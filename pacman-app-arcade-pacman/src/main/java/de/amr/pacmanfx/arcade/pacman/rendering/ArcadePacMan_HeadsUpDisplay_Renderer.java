/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.RectShort;
import javafx.scene.canvas.Canvas;

public class ArcadePacMan_HeadsUpDisplay_Renderer extends Arcade_HeadsUpDisplay_Renderer {

    public ArcadePacMan_HeadsUpDisplay_Renderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    protected RectShort[] bonusSymbolSprites() {
        return spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
    }

    @Override
    protected RectShort livesCounterSymbol() {
        return spriteSheet().findSprite(SpriteID.LIVES_COUNTER_SYMBOL);
    }
}