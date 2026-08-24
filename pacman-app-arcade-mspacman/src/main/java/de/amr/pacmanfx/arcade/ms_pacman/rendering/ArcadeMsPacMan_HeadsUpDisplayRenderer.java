/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_HeadsUpDisplay_Renderer;
import javafx.scene.canvas.Canvas;

public class ArcadeMsPacMan_HeadsUpDisplayRenderer extends Arcade_HeadsUpDisplay_Renderer {

    public ArcadeMsPacMan_HeadsUpDisplayRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
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