/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_GameRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.GameClock;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class PacManXXL_MsPacMan_GameRenderer extends ArcadeMsPacMan_GameRenderer {

    private final GenericMapRenderer mapRenderer;

    public PacManXXL_MsPacMan_GameRenderer(Canvas canvas, GameUI_Config uiConfig, ArcadeMsPacMan_SpriteSheet spriteSheet) {
        super(canvas, uiConfig, spriteSheet);
        mapRenderer = new GenericMapRenderer(canvas);
        mapRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void drawLevel(GameContext gameContext, GameClock gameClock, Color backgroundColor, boolean mazeBright, boolean energizerBright) {
        mapRenderer.drawLevel(gameContext.gameLevel(), mazeBright, energizerBright);
    }
}
