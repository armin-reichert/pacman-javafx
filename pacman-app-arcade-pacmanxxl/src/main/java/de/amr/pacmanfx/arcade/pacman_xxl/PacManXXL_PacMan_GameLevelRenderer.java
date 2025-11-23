/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GameLevel_Renderer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui._2d.GenericMapRenderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColorScheme;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.CONFIG_KEY_COLOR_MAP;

/**
 * Renderer for "Pac-Man XXL" game variant. Uses the vector graphics map renderer that can render any custom map.
 */
public class PacManXXL_PacMan_GameLevelRenderer extends ArcadePacMan_GameLevel_Renderer {

    private final GenericMapRenderer mazeRenderer;

    public PacManXXL_PacMan_GameLevelRenderer(Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas, spriteSheet, null);
        mazeRenderer = new GenericMapRenderer(canvas);
        mazeRenderer.scalingProperty().bind(scalingProperty());
        mazeRenderer.backgroundProperty().bind(backgroundProperty());
    }

    @Override
    public void applyLevelSettings(GameLevel gameLevel, RenderInfo info) {
        Map<String, String> colorMap = gameLevel.worldMap().getConfigValue(CONFIG_KEY_COLOR_MAP);
        var terrainMapColorScheme = new TerrainMapColorScheme(
            (Color) background(),
            Color.web(colorMap.get("fill")),
            Color.web(colorMap.get("stroke")),
            Color.web(colorMap.get("door"))
        );
        info.put("terrainMapColorScheme", terrainMapColorScheme);
    }

    @Override
    protected void drawMaze(GameLevel gameLevel, RenderInfo info) {
        mazeRenderer.drawMaze(gameLevel, info);
    }
}