/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GameLevel_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColorScheme;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Renderer for "Pac-Man XXL" game variant. Uses the vector graphics map renderer that can render any custom map.
 */
public class PacManXXL_PacMan_GameLevelRenderer extends ArcadePacMan_GameLevel_Renderer {

    private final GenericMapRenderer mazeRenderer;

    public PacManXXL_PacMan_GameLevelRenderer(Canvas canvas, ArcadePacMan_SpriteSheet spriteSheet) {
        super(canvas, spriteSheet, null);
        mazeRenderer = new GenericMapRenderer(canvas);
        mazeRenderer.scalingProperty().bind(scalingProperty());
        mazeRenderer.backgroundProperty().bind(backgroundProperty());
    }

    @Override
    public void applyLevelSettings(GameLevel gameLevel, RenderInfo info) {
        final WorldMapColorScheme worldMapColorScheme = gameLevel.worldMap().getConfigValue(GameUI_Config.CONFIG_KEY_COLOR_SCHEME);
        var terrainMapColorScheme = new TerrainMapColorScheme(
            (Color) background(),
            Color.web(worldMapColorScheme.walFill()),
            Color.web(worldMapColorScheme.wallStroke()),
            Color.web(worldMapColorScheme.door())
        );
        info.put("terrainMapColorScheme", terrainMapColorScheme);
    }

    @Override
    protected void drawMaze(GameLevel gameLevel, RenderInfo info) {
        mazeRenderer.drawMaze(gameLevel, info);
    }
}