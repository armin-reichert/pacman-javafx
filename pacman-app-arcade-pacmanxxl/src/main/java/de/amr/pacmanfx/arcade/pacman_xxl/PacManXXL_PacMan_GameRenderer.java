/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GameRenderer;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import de.amr.pacmanfx.uilib.GameClock;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Renderer for "Pac-Man XXL" game variant. Uses the vector graphics map renderer that can render any custom map.
 */
public class PacManXXL_PacMan_GameRenderer extends ArcadePacMan_GameRenderer {

    private final GenericMapRenderer mapRenderer;

    public PacManXXL_PacMan_GameRenderer(PacManXXL_PacMan_UIConfig uiConfig, Canvas canvas) {
        super(uiConfig, canvas, uiConfig.spriteSheet());
        mapRenderer = new GenericMapRenderer(canvas);
        mapRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void drawGameLevel(GameContext gameContext, GameClock gameClock, Color backgroundColor, boolean mazeBright, boolean energizerBright) {
        mapRenderer.drawLevel(gameContext.gameLevel(), mazeBright, energizerBright);
    }
}