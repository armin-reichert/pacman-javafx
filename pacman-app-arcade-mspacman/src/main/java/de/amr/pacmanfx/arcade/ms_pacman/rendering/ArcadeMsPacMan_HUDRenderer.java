/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_HUDRenderer;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_HUDRenderer extends ArcadePacMan_HUDRenderer {

    public ArcadeMsPacMan_HUDRenderer(GameUI_Config uiConfig, Canvas canvas, ArcadeMsPacMan_SpriteSheet spriteSheet) {
        super(uiConfig, canvas);
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    protected RectShort[] bonusSymbols() {
        var ss = (ArcadeMsPacMan_SpriteSheet) spriteSheet;
        return ss.spriteSequence(SpriteID.BONUS_SYMBOLS);
    }

    protected RectShort livesCounterSymbol() {
        var ss = (ArcadeMsPacMan_SpriteSheet) spriteSheet;
        return ss.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
    }
}
