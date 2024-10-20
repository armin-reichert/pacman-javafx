/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE;
import static de.amr.games.pacman.ui2d.GameAssets2D.ARCADE_PALE;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

    @Override
    public void init() {
        context.setScoreVisible(false);
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
        if (context.gameState().timer().atSecond(4)) {
            context.gameController().terminateCurrentState();
        }
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE;
    }

    @Override
    public void draw(GameRenderer renderer) {
        renderer.setScaling(scaling());
        renderer.setBackgroundColor(backgroundColor());
        var timer = context.gameState().timer();
        if (timer.currentTick() == 1) {
            renderer.clearCanvas();
        } else if (timer.betweenSeconds(1, 2) && timer.currentTick() % 8 == 0) {
            paintRandomHexCodes(renderer, size());
        } else if (timer.betweenSeconds(2, 3.5) && timer.currentTick() % 4 == 0) {
            paintRandomSprites(renderer, size());
        } else if (timer.atSecond(3.5)) {
            paintGrid(renderer, size());
        }
    }

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        // not used
    }

    private void paintRandomHexCodes(GameRenderer renderer, Vector2f sceneSize) {
        renderer.clearCanvas();
        renderer.ctx().setFill(ARCADE_PALE);
        renderer.ctx().setFont(renderer.scaledArcadeFont(TS));
        int numRows = (int) (sceneSize.y() / TS), numCols = (int) (sceneSize.x() / TS);
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                var hexCode = Integer.toHexString(RND.nextInt(16));
                renderer.ctx().fillText(hexCode, scaled(t(col)), scaled(t(row + 1)));
            }
        }
    }

    private void paintRandomSprites(GameRenderer renderer, Vector2f sceneSize) {
        GameSpriteSheet spriteSheet = context.currentGameSceneConfig().spriteSheet();
        renderer.clearCanvas();
        int numRows = (int) (sceneSize.y() / TS), numCols = (int) (sceneSize.x() / TS);
        for (int row = 0; row < numRows / 2; ++row) {
            if (RND.nextInt(100) > 20) {
                var region1 = randomSpriteSheetTile(spriteSheet);
                var region2 = randomSpriteSheetTile(spriteSheet);
                var splitX = numCols / 8 + RND.nextInt(numCols / 4);
                for (int col = 0; col < numCols / 2; ++col) {
                    var region = col < splitX ? region1 : region2;
                    renderer.drawSpriteScaled(region, region.width() * col, region.height() * row);
                }
            }
        }
    }

    private RectArea randomSpriteSheetTile(GameSpriteSheet spriteSheet) {
        var source = spriteSheet.sourceImage();
        var raster = 16;
        int x = (int) (RND.nextDouble() * ((int) source.getWidth() - raster));
        int y = (int) (RND.nextDouble() * ((int) source.getHeight() - raster));
        return new RectArea(x, y, raster, raster);
    }

    private void paintGrid(GameRenderer renderer, Vector2f sceneSize) {
        renderer.clearCanvas();
        double width = t(28), height = t(36), raster = 16;
        var numRows = sceneSize.y() / TS / 2;
        var numCols = sceneSize.x() / TS / 2;
        renderer.ctx().setStroke(ARCADE_PALE);
        renderer.ctx().setLineWidth(scaled(2.0));
        for (int row = 0; row <= numRows; ++row) {
            renderer.ctx().setLineWidth(row == 0 || row == numRows ? scaled(4.0) : scaled(2.0));
            renderer.ctx().strokeLine(0, scaled(row * raster), scaled(width), scaled(row * raster));
        }
        for (int col = 0; col <= numCols; ++col) {
            renderer.ctx().setLineWidth(col == 0 || col == numCols ? scaled(4.0) : scaled(2.0));
            renderer.ctx().strokeLine(scaled(col * raster), 0, scaled(col * raster), scaled(height));
        }
    }
}